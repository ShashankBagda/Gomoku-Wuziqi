#!/usr/bin/env bash

# Be strict on unset vars and pipelines; handle non-zero exits manually
set -u -o pipefail

TARGET_URL="${1:-http://127.0.0.1:3000}"
OUTPUT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPORT_FILE="${OUTPUT_DIR}/zap-baseline-report.html"

if ! command -v docker >/dev/null 2>&1; then
  echo "❌ Docker is required to run the OWASP ZAP baseline scan." >&2
  exit 1
fi

echo "🔍 Running OWASP ZAP baseline scan against ${TARGET_URL}"
echo "📄 Report will be saved to ${REPORT_FILE}"

set +e
docker run --rm \
  -e HOME=/zap/wrk \
  -w /zap/wrk \
  -v "${OUTPUT_DIR}:/zap/wrk" \
  ghcr.io/zaproxy/zaproxy:stable \
  zap-baseline.py \
  -t "${TARGET_URL}" \
  -m 5 \
  -r "$(basename "${REPORT_FILE}")"
status=$?
set -e

if [[ "${DAST_STRICT:-false}" == "true" ]]; then
  echo "DAST_STRICT=true set; exiting with ZAP status ${status}"
  exit ${status}
fi

if [[ ${status} -ne 0 ]]; then
  echo "⚠️ ZAP returned non-zero status (${status}). Continuing pipeline; see report at ${REPORT_FILE}" >&2
else
  echo "✅ Scan completed. Report: ${REPORT_FILE}"
fi
