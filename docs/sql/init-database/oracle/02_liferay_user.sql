-- Change session to Pluggable DataBase (PDB)
ALTER SESSION SET container = ORCLPDB1;

-- Create user for new schema Liferay Dev
CREATE USER liferay IDENTIFIED BY liferay DEFAULT TABLESPACE
liferay_data TEMPORARY TABLESPACE liferay_temp PROFILE
DEFAULT account unlock;
