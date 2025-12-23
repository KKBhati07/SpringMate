# Local HTTPS Configuration

This project uses **HTTPS only for local development**.

## ❌ Do NOT commit keystore files
Keystore files contain private keys and must remain local.

Ignored files include:
- *.p12
- *.jks

## ✅ Local setup (development only)

1. Generate local certificates using `mkcert`
2. Convert to PKCS12 format:
   ```bash
   openssl pkcs12 -export \
     -in wildcard.marketmate.local.pem \
     -inkey wildcard.marketmate.local-key.pem \
     -out marketmate-local.p12 \
     -name marketmate \
     -password pass:changeit
