# Cricket Pro AI Android

Phone-only Android version of Cricket Pro AI professional live analytics.

## V1 capabilities

- Direct no-key CREX public-page collector
- Automatic live-match discovery and 15-second refresh
- On-device deterministic probabilistic analytics
- Central, 50% and 80% final-score ranges
- Next-over/two-over, wicket and boundary probabilities
- Chase win probability and coach explanation
- Feed status, timestamp and missing-data transparency

## APK build

GitHub Actions builds `app-debug.apk` on every branch push. Open Actions,
select the latest **Build Android APK** run, and download the
**Cricket-Pro-AI-debug-APK** artifact.

The public-page parser is intentionally transparent: if CREX changes its HTML,
the app reports that the score markup was not recognized instead of displaying
stale or invented data.
