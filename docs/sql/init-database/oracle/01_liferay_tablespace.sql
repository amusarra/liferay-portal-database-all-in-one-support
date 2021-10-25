-- Change session to Pluggable DataBase (PDB)
ALTER SESSION SET container = ORCLPDB1;

-- Create tablespace for schema Liferay
CREATE TABLESPACE liferay_data logging DATAFILE
'/opt/oracle/oradata/ORCLCDB/liferay_data_1.dbf' SIZE 64m
autoextend ON NEXT 32m maxsize 4096m blocksize 8k EXTENT management local;
 
--Create temp tablespace for Liferay
CREATE TEMPORARY TABLESPACE liferay_temp tempfile 
'/opt/oracle/oradata/ORCLCDB/liferay_temp_1.dbf' 
SIZE 64m autoextend ON NEXT 32m maxsize 2048m blocksize 8k 
EXTENT management local;
