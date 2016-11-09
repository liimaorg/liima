#!/bin/bash
#This script generates the liquibase-xml for adding a new permission and assing this permission to an existing role
PERMISSION_NAME=$1
ROLE_NAME=$2

if test -n $PERMISSION_NAME -o -n $ROLE_NAME; then
  echo permission name and role name are required.
else
printf "<!-- Create new permission -->
<insert tableName=\"TAMW_PERMISSION\">
        <column name=\"ID\" valueComputed=\"(SELECT NEXT_VAL FROM SAMW_SEQUENCES WHERE SEQ_NAME = 'permissionId')\" />
        <column name=\"VALUE\" value=\"$PERMISSION_NAME\"></column>
        <column name=\"V\" valueNumeric=\"0\" />
</insert>\n\n"

printf "<!-- Assign new permission to role -->
<insert tableName=\"TAMW_ROLE_PERMISSION\">
        <column name=\"ROLES_ID\" valueComputed=\"(SELECT ID FROM TAMW_ROLE WHERE NAME = '$ROLE_NAME')\" />
        <column name=\"PERMISSIONS_ID\" valueComputed=\"(SELECT ID FROM TAMW_PERMISSION WHERE VALUE = '$PERMISSION_NAME')\" />
</insert>\n\n"

printf "<!-- Update the Id sequence -->
<update tableName=\"SAMW_SEQUENCES\">
        <column name=\"NEXT_VAL\" type=\"INTEGER\"
        valueComputed=\"(SELECT NEXT_VAL+1 FROM SAMW_SEQUENCES WHERE SEQ_NAME='permissionId')\" />
        <where>SEQ_NAME='permissionId'</where>
</update>\n\n"
fi