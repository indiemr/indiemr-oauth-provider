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

Set these global properties in OpenMRS Administration:

| Property | Description |
|----------|-------------|
| `indiemroauthprovider.publicBaseUrl` | Public host URL (e.g. `https://your-host`) |
| `indiemroauthprovider.encKey` | Hex encryption key for tokens and OAuth state |
| `indiemroauthprovider.google.clientId` | Google OAuth client ID |
| `indiemroauthprovider.google.clientSecret` | Google OAuth client secret |
| `indiemroauthprovider.google.redirectUri` | Callback URL: `{publicBaseUrl}/openmrs/ws/rest/v1/teleconsult/connect/callback` |

## REST API

Base path: `/openmrs/ws/rest/v1/teleconsult`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/connect-url` | Yes | Get OAuth authorization URL |
| GET | `/check-token` | Yes | Check stored OAuth token status |
| GET | `/connect/callback` | No | OAuth callback (Google redirect) |
| POST | `/mint` | Yes | Create meeting + patient link |
| POST | `/events` | Yes | Create calendar event |
| DELETE | `/appointments/{uuid}/resources` | Yes | Cancel appointment resources |
| GET | `/link/{token}` | No | Public patient landing page |

## Integration tests

```bash
chmod +x tests/integration-test.sh
./tests/integration-test.sh
```
