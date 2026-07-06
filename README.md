# IndiEMR OAuth Provider Module

OpenMRS module (`indiemroauthprovider`) for OAuth connect, Google Calendar/Meet integration, and patient teleconsult links.

## Build

```bash
mvn clean install
```

Output: `omod/target/indiemroauthprovider-1.0.0-SNAPSHOT.omod`

## GitHub Packages

Artifacts are published to GitHub Packages on push to `main` (snapshots) and on version tags (releases).

### Consuming from another repo

Add the repository and dependency to your `pom.xml`:

```xml
<repository>
  <id>github-indiemr-oauth-provider</id>
  <url>https://maven.pkg.github.com/indiemr/indiemr-oauth-provider</url>
  <snapshots>
    <enabled>true</enabled>
  </snapshots>
</repository>
```

```xml
<!-- API jar -->
<dependency>
  <groupId>org.openmrs.module</groupId>
  <artifactId>indiemroauthprovider-api</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>

<!-- OMOD for distro builds -->
<dependency>
  <groupId>org.openmrs.module</groupId>
  <artifactId>indiemroauthprovider-omod</artifactId>
  <version>1.0.0-SNAPSHOT</version>
  <type>omod</type>
</dependency>
```

Authenticate in `~/.m2/settings.xml` (or via `actions/setup-java` in CI) with a GitHub token that has `read:packages`:

```xml
<server>
  <id>github-indiemr-oauth-provider</id>
  <username>YOUR_GITHUB_USERNAME</username>
  <password>YOUR_GITHUB_TOKEN</password>
</server>
```

The server `id` must match the repository `id` in your `pom.xml`.

## Deploy

Copy the `.omod` file to your OpenMRS `modules/` directory and restart.

## Configuration

Configuration is loaded from environment variables and/or an `application.yml` file (not OpenMRS global properties).

**Option A — environment variables** (recommended for production):

| Variable | Description |
|----------|-------------|
| `INDIEMR_OAUTH_PUBLIC_BASE_URL` | Public host URL (e.g. `https://your-host`) |
| `INDIEMR_OAUTH_ENC_KEY` | Hex encryption key for tokens and OAuth state |
| `INDIEMR_OAUTH_GOOGLE_CLIENT_ID` | Google OAuth client ID |
| `INDIEMR_OAUTH_GOOGLE_CLIENT_SECRET` | Google OAuth client secret |
| `INDIEMR_OAUTH_GOOGLE_REDIRECT_URI` | Callback URL: `{publicBaseUrl}/openmrs/ws/rest/v1/oauth/connect/callback` |

**Option B — YAML file** at `{OPENMRS_APPLICATION_DATA_DIRECTORY}/indiemroauthprovider/application.yml`, or set `INDIEMR_OAUTH_CONFIG_FILE` to a custom path. See `api/src/main/resources/application.yml.example`. Environment variables override file values.

## REST API

### OAuth — `/openmrs/ws/rest/v1/oauth`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/connect-url` | Yes | Get OAuth authorization URL |
| GET | `/check-token` | Yes | Check stored OAuth token status |
| GET | `/connect/callback` | No | Google OAuth redirect callback |

### Teleconsult — `/openmrs/ws/rest/v1/teleconsult`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/events` | Yes | Create calendar event (optionally with Meet + join link) |
| PUT | `/events` | Yes | Reschedule or update an existing calendar event |
| DELETE | `/appointments/{uuid}/resources` | Yes | Cancel appointment resources |
| GET | `/link/{token}` | No | Public patient landing page |

### `POST /events` payload

Calendar-only:

```json
{
  "oauthProviderCode": "GOOGLE",
  "title": "Appointment - John Doe",
  "resourceType": "APPOINTMENT",
  "resourceUuid": "appointment-uuid",
  "start": "2026-07-02T10:00:00.000Z",
  "end": "2026-07-02T11:00:00.000Z",
  "timeZone": "UTC",
  "createMeet": false
}
```

Calendar + Google Meet + shareable join link:

```json
{
  "oauthProviderCode": "GOOGLE",
  "title": "Appointment - John Doe - +91 xxxxx",
  "resourceType": "APPOINTMENT",
  "resourceUuid": "appointment-uuid",
  "start": "2026-07-02T10:00:00.000Z",
  "end": "2026-07-02T11:00:00.000Z",
  "timeZone": "Asia/Kolkata",
  "createMeet": true,
  "mintJoinLink": true
}
```

Response fields: `resourceUuid`, `externalEventId`, `htmlLink`, and when Meet is created: `meetingUrl`, `joinToken`, `resolverUrl`.

`PUT /events` — partial update by `resourceType` + `resourceUuid` (at least one of `title`, `description`, `start`, `end`, `timeZone`):

```json
{
  "resourceType": "APPOINTMENT",
  "resourceUuid": "appointment-uuid",
  "title": "Rescheduled - John Doe",
  "start": "2026-07-03T04:30:00.000Z",
  "end": "2026-07-03T05:00:00.000Z",
  "timeZone": "Asia/Kolkata"
}
```

If `end` is updated and a join link exists, its expiry is extended accordingly.

## Integration tests

```bash
chmod +x tests/integration-test.sh
./tests/integration-test.sh
```
