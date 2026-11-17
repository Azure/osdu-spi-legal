# Release notes

## Breaking changes — stricter JSON on `POST /legaltags`

Creating a LegalTag now enforces the OpenAPI request contract at runtime. Payloads that previously succeeded can return **HTTP 400**.

### Unknown properties

Previously, create used `LegalTagDto` with `ignoreUnknown = true`, so extra JSON fields were ignored.

Now:

- Unknown **top-level** fields on the create body return **400**.
- Unknown fields **inside** `properties` return **400** (`additionalProperties: false` on create properties).

**Exception (compatibility):** server-assigned read-only fields from the shared `LegalTag` model — `isValid` and `id` — are **ignored** when present (clients such as os-core-test that serialize the full model keep working).

**Migration:** send only documented create fields (`name`, `description`, `properties` and the allowed property keys). Stop sending response-only or client-local extras other than `isValid` / `id`.

### Explicit JSON `null`

Omitted optional fields remain allowed. An **explicit** `null` for a non-nullable field is rejected with **400**, for example:

- `"description": null`
- `"properties": { "expirationDate": null, ... }` (and other non-nullable property values)

**Migration:** omit the field instead of sending `null`, or send a valid typed value.

### Related strictness (same release)

JSON types must match the schema (no coercion). For example `"contractId": 12345` or `"originator": false` return **400**; use strings where the schema requires strings. See [API](api.md#headers).

## Breaking change — `PUT /legaltags` now preserves omitted fields

Update is now partial. Only `name` is required. `contractId`, `expirationDate`, `description`, and `extensionProperties` stay optional.

On master the request replaced those fields:

- An omitted `description` was stored as `""`.
- An omitted `contractId`, `expirationDate`, or `extensionProperties` was stored as null. Omitting `expirationDate` cleared it.

An omitted field now keeps the stored value. JSON `null` for `expirationDate` is treated as omitted, so a stored expiration date cannot be cleared. Send a new `YYYY-MM-DD` value to change it.

**Migration:** clients that sent a partial body and expected the missing fields to be cleared must send the values they want stored. There is no request value that removes an existing `expirationDate`.
