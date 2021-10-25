-- Change session to Pluggable DataBase (PDB)
ALTER SESSION SET container = ORCLPDB1;

-- Assign grant to Liferay User
GRANT CONNECT TO liferay;
GRANT RESOURCE TO liferay;
GRANT UNLIMITED TABLESPACE TO liferay;
