import json
import os
from datetime import datetime, timezone


def handler(event, context):
    processed = []
    for record in event.get("Records", []):
        s3 = record.get("s3", {})
        bucket = s3.get("bucket", {}).get("name")
        key = s3.get("object", {}).get("key")
        processed.append({
            "bucket": bucket,
            "key": key,
            "processedAt": datetime.now(timezone.utc).isoformat(),
            "status": "metadata-captured",
        })

    return {
        "statusCode": 200,
        "body": json.dumps({
            "environment": os.getenv("ENVIRONMENT", "dev"),
            "processed": processed,
        }),
    }
