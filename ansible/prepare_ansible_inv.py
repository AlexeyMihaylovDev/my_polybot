import json


def get_instance_name(tags):
    for tag in tags:
        if tag['Key'] == 'Name':
            return tag['Value']



def prepare_ansible_inventory():
    with open('hosts.json') as f:
        instances = json.load(f)

    hosts = []
    for instance in instances:
        instance_name = get_instance_name(instance['Tags'])
        instance_ip = instance.get('PublicIpAddress')
        if instance_name == "alexey-bot" :
            hosts.append(
            f"{instance_name} ansible_host={instance_ip}\n"
            )
    if len(hosts)==0 : return print("No found Instances by tags name alexey-bot")

    with open('hosts', 'w') as f:
        f.write('[bot]\n')
        f.writelines(hosts)


if __name__ == '__main__':

    prepare_ansible_inventory()