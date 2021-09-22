import sys
from flask import Flask


app = Flask(__name__)


@app.route('/')
def hello():
    return 'Hello, World!'


if __name__ == '__main__':
    port = int(sys.argv[1]) if len(sys.argv) > 1 else 80
    app.run(host='0.0.0.0', port=port)
