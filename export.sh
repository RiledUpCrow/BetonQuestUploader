USER="Co0sh"
PASSWORD="some_password"
SERVER="localhost"
PORT="8125"

# DON'T TOUCH ANYTHING BELOW

java -jar BetonQuestUploader.jar http://$SERVER:$PORT $USER $PASSWORD $1
rm $1