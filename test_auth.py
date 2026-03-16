import requests

print("Testing JSON payload:")
res = requests.post("https://vitascan-ai-backend.onrender.com/auth/login", json={"email": "test@test.com", "password":"test"})
print(f"Status: {res.status_code}")
print(f"Response: {res.text}\n")

print("Testing Form payload:")
res = requests.post("https://vitascan-ai-backend.onrender.com/auth/login", data={"email": "test@test.com", "password":"test"})
print(f"Status: {res.status_code}")
print(f"Response: {res.text}\n")
