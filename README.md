# IndiEMR OAuth Provider Module

OpenMRS module (`indiemroauthprovider`) for OAuth connect, Google Calendar/Meet integration, and patient teleconsult links.

## Build

```bash
mvn clean install
```

Output: `omod/target/indiemroauthprovider-1.0.0-SNAPSHOT.omod`

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
| POST | `/mint` | Yes | Create meeting + patient link |
| POST | `/events` | Yes | Create calendar event |
| DELETE | `/appointments/{uuid}/resources` | Yes | Cancel appointment resources |
| GET | `/link/{token}` | No | Public patient landing page |

### Request payloads

`POST /mint` example:

```json
{
  "oauthProviderCode": "GOOGLE",
  "title": "Appointment - John Doe - +91 xxxxx",
  "resourceType": "APPOINTMENT",
  "resourceUuid": "appointment-uuid"
}
```

`POST /events` is generic and caller-driven.

```json
{
  "oauthProviderCode": "GOOGLE",
  "title": "Appointment - John Doe - +91 xxxxx",
  "resourceType": "APPOINTMENT",
  "resourceUuid": "appointment-uuid",
  "start": "2026-07-02T10:00:00.000Z",
  "end": "2026-07-02T11:00:00.000Z",
  "timeZone": "UTC"
}
```

`POST /events` requires `title`, `resourceType`, and `resourceUuid`; no `resources` array is needed for this API.

## Integration tests

```bash
chmod +x tests/integration-test.sh
./tests/integration-test.sh
```
