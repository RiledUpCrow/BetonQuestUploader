SET user=Co0sh
SET password=some_password
SET server=localhost
SET port=8125

rem DON'T TOUCH ANYTHING BELOW

java -jar BetonQuestUploader.jar http://%server%:%port% %user% %password% %1
del %1