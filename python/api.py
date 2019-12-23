from flask import Flask, request
from flask_restful import Resource, Api
from json import dumps
import random

app = Flask(__name__)
api = Api(app)

Switch = {
	"cpu1": 10,
	"cpu2": 12,
	"cpu3": 14,
	"cpu4": 16,
	"cpu5": 18,
	"cpu6": 20
}	

class Usage(Resource):
	def get(self, switchID):
		x = "10"
		print(switchID)
		if (switchID > 0 and switchID < 7):
			return Switch["cpu" + str(switchID)]  * random.randint(1, 5)
		else:
			return "error"


api.add_resource(Usage, "/usage/<int:switchID>") # Route_1

if __name__ == '__main__':
     app.run(port='5002')