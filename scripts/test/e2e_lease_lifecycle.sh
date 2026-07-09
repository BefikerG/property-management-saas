#!/bin/bash
# 1. Login to get token
ACCESS_TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@addisprime.com", "password": "Admin1234!"}' \
  | jq -r '.accessToken')
echo "Token obtained."

# Use the unit ID the user was working with
UNIT_ID="ad4c8979-425c-41af-97e3-35b2ff0fa04f"

# 2. Create a fresh tenant profile
echo -e "\n--- Creating Tenant Profile ---"
TENANT_PROFILE_ID=$(curl -s -X POST http://localhost:8080/api/v1/tenant-profiles \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -d '{
    "fullName": "Test Renter",
    "email": "test.renter'$(date +%s)'@example.com"
  }' | jq -r '.id')
echo "Tenant Profile created with ID: $TENANT_PROFILE_ID"

# 3. Create first lease
echo -e "\n--- Creating First Lease (DRAFT) ---"
LEASE_1_ID=$(curl -s -X POST http://localhost:8080/api/v1/leases \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -d '{
    "unitId": "'$UNIT_ID'",
    "tenantProfileId": "'$TENANT_PROFILE_ID'",
    "startDate": "2026-09-01",
    "endDate": "2027-08-31",
    "monthlyRent": 48000.00,
    "billingDay": 1
  }' | jq -r '.id')
echo "First Lease created with ID: $LEASE_1_ID"

# 4. Activate first lease
echo -e "\n--- Activating First Lease ---"
curl -s -X POST http://localhost:8080/api/v1/leases/$LEASE_1_ID/activate \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq

# 5. Attempt second lease on same unit
echo -e "\n--- Attempting Second Lease (Should be 409) ---"
curl -s -X POST http://localhost:8080/api/v1/leases \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -d '{
    "unitId": "'$UNIT_ID'",
    "tenantProfileId": "'$TENANT_PROFILE_ID'",
    "startDate": "2026-10-01",
    "endDate": "2027-09-30",
    "monthlyRent": 50000.00,
    "billingDay": 1
  }' | jq

