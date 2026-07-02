#!/bin/bash
#
# Integration tests for indiemroauthprovider OMOD
# Runs curl commands against a live OpenMRS instance.
#
# Usage:
#   chmod +x tests/integration-test.sh
#   ./tests/integration-test.sh
#
# Optional environment variables:
#   BASE_URL    (default: https://localhost)
#   USER_CREDS  (default: JohnDoe:e2e@Test)

set -euo pipefail

BASE_URL="${BASE_URL:-https://localhost}"
API="$BASE_URL/openmrs/ws/rest/v1/teleconsult"
USER_CREDS="${USER_CREDS:-JohnDoe:e2e@Test}"
CURL="curl -sk -u $USER_CREDS"

PASS=0
FAIL=0

assert_http() {
    local test_name="$1"
    local expected_code="$2"
    local actual_code="$3"

    if [ "$actual_code" = "$expected_code" ]; then
        echo "  PASS  $test_name"
        PASS=$((PASS + 1))
    else
        echo "  FAIL  $test_name (expected HTTP $expected_code, got $actual_code)"
        FAIL=$((FAIL + 1))
    fi
}

echo "=== Checking OpenMRS connectivity ==="
HTTP_CODE=$(curl -sk -u "$USER_CREDS" -o /dev/null -w "%{http_code}" "$BASE_URL/openmrs/ws/rest/v1/session")
if [ "$HTTP_CODE" != "200" ]; then
    echo "FATAL: Cannot connect to OpenMRS at $BASE_URL (HTTP $HTTP_CODE)"
    exit 1
fi
echo "  Connected"
echo ""

echo "=== 1. Unauthenticated check-token → 401 ==="
HTTP_CODE=$(curl -sk -o /dev/null -w "%{http_code}" "$API/check-token")
assert_http "1.1 Unauthenticated check-token" "401" "$HTTP_CODE"

echo "=== 2. Authenticated check-token → 200 ==="
HTTP_CODE=$(curl -sk -u "$USER_CREDS" -o /dev/null -w "%{http_code}" "$API/check-token")
assert_http "2.1 Authenticated check-token" "200" "$HTTP_CODE"

echo "=== 3. Connect URL → 200 ==="
HTTP_CODE=$(curl -sk -u "$USER_CREDS" -o /dev/null -w "%{http_code}" \
    "$API/connect-url?providerDisplay=Test%20Doctor&oauthProvider=GOOGLE")
assert_http "3.1 Connect URL" "200" "$HTTP_CODE"

echo ""
TOTAL=$((PASS + FAIL))
echo "RESULTS: $PASS/$TOTAL passed"
if [ "$FAIL" -gt 0 ]; then
    exit 1
fi
