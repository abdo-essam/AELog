# Security Policy

## Supported Versions

| Version | Supported          |
|---------|--------------------|
| 1.x.x  | ✅ Active          |
| < 1.0   | ❌ Not supported   |

## Reporting a Vulnerability

**Please do NOT open a public GitHub issue for security vulnerabilities.**

Instead, please report them via:
- Email: security@ae.com
- GitHub Security Advisories: [Report](https://github.com/abdo-essam/AELog/security/advisories/new)

### What to include
- Description of the vulnerability
- Steps to reproduce
- Impact assessment
- Suggested fix (if any)

### Response Timeline
- **Acknowledgement**: Within 48 hours
- **Initial Assessment**: Within 1 week
- **Fix Release**: Within 2 weeks for critical issues

## Scope

This policy applies to the AELog library code. 
Since this is a debug-only tool, the primary concern is:
- Accidental inclusion in release builds leaking sensitive log data
- Plugin system being exploitable to execute arbitrary code
