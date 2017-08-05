import sys, json;

print("Score")
for score in json.load(sys.stdin)['stop']['scores']: 
    print("p{0}{1}->{2}".format(score['punter'], score['name'], score['score']))
