from flask import Flask
from flask import request
global datalist

app = Flask(__name__)
datalist = dict()

@app.route('/get')
def get():
	print "Hii"
	data = request.args.get('phone','default')
	print "Hiii"
	total = "|".join(["-".join(otherData) for phone,otherData in datalist.items() if data != phone])
	print "Hiiii"
	return total

@app.route('/post')
def post():
	data = request.args.get('data','default').split("-")
	datalist[data[0]] = data[1:]
	return "Success"

if __name__ == "__main__":
	app.run()