# Duplicate relation cleanup

## Why this is needed

Adding the same related resource more than once in Angular could create duplicate
database relations with the same name. The UI groups these rows and may show only
one relation, hiding the duplicates and their potentially different configuration.
Existing duplicates also prevent the database uniqueness constraints from being
installed.

This tool consolidates each duplicate group into one relation, retaining its
configuration and merging entries from the other rows. Conflicting definitions
are resolved using the policy below and reported for review. Run the cleanup
before installing the uniqueness constraints.

## How it works

Deploy the backend endpoint first:
`POST /resources/relations/merge-duplicates` (relative to the REST
application root).

The endpoint discovers all duplicate groups and reads their configuration directly
from the application database. The whole migration runs in **one transaction**:
any failure rolls back every group. The lowest-ID relation in each group is
retained with its name, type and exact release coverage. Resources and releases
are not deleted.

Only rows with identical mode (consumed or provided), master, physical slave
resource and raw identifier (including NULL) are merged. Dry run is the default.
Apply is explicitly selected with `dryRun=false`; it does not require a previous
dry run or a confirmation token. Each invocation uses the current database state.

## Conflict policy

Relations are processed in ascending ID order. Existing entries win; missing
entries are filled from later peers. Properties are matched by technical name
within an environment, templates by name within an environment, and activations
by application resource-group ID within an environment. Descriptors are matched
by technical name across the group; the earliest declaration's metadata and tags
win. Environment IDs and then configuration row IDs break ties within a relation.
Every discarded entry is reported, even if equivalent, without its value.
An explicitly stored NULL/empty property value counts as defined.

Local descriptors (including metadata, types and tags), local property values,
templates (including contents, target paths and platforms) and explicit
activations are merged. Inherited configuration is not materialized. Encrypted
values stay encrypted in the backend; responses contain no property values or keys.
Only when conflicting descriptor encryption flags require conversion does Liima
decrypt/re-encrypt internally, using its configured key. Missing/incorrect keys
abort the transaction. These conversions are explicitly reported in preview;
the first descriptor's flag wins, so this can include an encryption downgrade.
Review those entries before applying. Existing audit history remains, with new
revisions for the replacement contexts/configuration and deleted surplus relations.

Malformed ownership (e.g. a relation-local descriptor used outside the duplicate
group, a context owned by both relation modes, or repeated environment contexts)
aborts rather than silently deleting unrelated data. Unexpected DB constraints,
permission failures and persistence errors also roll back the whole migration.
This handles the supported configuration categories, not arbitrary corrupt schemas.

## Running from the REST UI

1. Back up the DB and test on a restored environment. Pause **all writers** from
   dry run through apply; master/relation locks do not cover every other edit
   path. The caller needs resource update permission in Global and every affected
   environment for every affected master.
2. Open Swagger and select **Preview or apply the complete duplicate-relation
   migration** under `/resources/relations`.
3. Execute with `dryRun=true` (the default), without a request body:

   ```text
   POST /resources/relations/merge-duplicates?dryRun=true
   ```

   The response covers the entire migration: `groupCount`,
   `surplusRelationCount` and `groups` with source/target IDs, mode, identifier,
   retained ID, all original relation IDs and discarded alternatives.
   `applied` is false and `auditRevisionId` is null. No configuration or audit
   changes are persisted.
4. Review the retained IDs, discarded alternatives and encryption conversions.
   To apply, execute without a request body:

   ```text
   POST /resources/relations/merge-duplicates?dryRun=false
   ```

   A successful response has `applied=true`; all groups have committed together.
   Save the response as the migration report. It contains the Envers
   `auditRevisionId` when changes were made. A configuration conflict returns
   HTTP 409; permission and persistence failures also abort the whole transaction.
   No groups are committed if any group fails.
5. Execute another dry run to confirm `groupCount=0`. Repeating apply after a
   successful migration is a no-op. Restart application instances if needed to
   clear caches, then resume writers.

The migration shares one Envers revision across all changed resources. A
single-master migration associates that revision with its master. When several
masters are affected, the revision has no single resource owner, so the existing
per-resource audit view does not list it. Use the returned `auditRevisionId` to
identify the migration's audit records; changes are not attributed to an
arbitrarily selected master.

Ensure server transaction and proxy/request timeouts allow the entire migration
to finish. A timeout or lost connection leaves the outcome unconfirmed: the
server might still be running or might have committed. Verify that the original
request has finished, then use a dry run and inspect audit records before retrying.
Responses contain discarded keys, not their values; the database backup and
audit history are required for value recovery.
