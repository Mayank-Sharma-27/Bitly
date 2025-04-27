High Level diagram


![HLD TinyURL](High_Level_Design_TinyUrl.png)


We will have 2 redis fleet
1. To get the redirect url from the tiny url
2. To check if we already have created the long url shortner (This is only done if require)

Our dynmodb will have the short url as the primary key

Suppose:

1 billion (1,000,000,000) mappings

Each URL (long and short) average size:

Field	Assumption
Long URL	~100 bytes (average URL)
Short URL	~8 characters (base62), let's say ~10 bytes with Redis metadata


You have 1 billion mappings * 2 keys (long ➔ short + short ➔ long):

java
Copy
Edit
Number of keys = 2 * 1,000,000,000 = 2 billion keys
Memory = 2B * 160 bytes = 320,000,000,000 bytes
= 320 GB
✅ 320 GB RAM needed for Redis to fully cache 1 billion URL mappings in both directions.


We can use multiple redis instance to store this much data.
