import json

with open('exam.json', 'r') as fcc_file:
    fcc_data = json.load(fcc_file)
    # print(json.dumps(fcc_data, indent=4))
    print(fcc_data['public_ip']['value'])

with open('readme.txt', 'w') as f:
    f.write(fcc_data['public_ip']['value'])