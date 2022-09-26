import json
import time
import boto3
import botocore
import os
from loguru import logger
from utils import search_download_youtube_video


def process_msg(msg):
    downloaded_videos = search_download_youtube_video(msg)



    # TODO upload the downloaded video to your S3 bucket
    for index, video in enumerate(downloaded_videos, start=1):
        s3 = boto3.client('s3')
        s3.upload_file(video, 'alexey-dima-polybot-bucket', video)
        os.remove(f'./{video}')


def main():
    while True:
        try:
            messages = queue.receive_messages(
                MessageAttributeNames=['All'],
                MaxNumberOfMessages=1,
                WaitTimeSeconds=10
            )
            for msg in messages:
                logger.info(f'processing message {msg}')
                process_msg(msg.body)
                logger.info(f'task messege id: {msg.message_id}')
                # delete message from the queue after is was handled
                response = queue.delete_messages(Entries=[{
                    'Id': msg.message_id,
                    'ReceiptHandle': msg.receipt_handle
                }])
                if 'Successful' in response:
                    logger.info(f'msg {msg} has been handled successfully')

        except botocore.exceptions.ClientError as err:
            logger.exception(f"Couldn't receive messages {err}")
            time.sleep(10)


if __name__ == '__main__':
    with open('config.json') as f:
        config = json.load(f)

    sqs = boto3.resource('sqs', region_name=config.get('aws_region'))
    queue = sqs.get_queue_by_name(QueueName=config.get('bot_to_worker_queue_name'))

    main()
