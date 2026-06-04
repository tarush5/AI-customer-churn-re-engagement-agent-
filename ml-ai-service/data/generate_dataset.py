"""
generate_dataset.py — Generates a synthetic retail customer CSV dataset (~500 customers)
for the RetainIQ churn prediction system.
"""

import csv
import random
import os
from datetime import datetime, timedelta

# Seed for reproducibility
random.seed(42)

FIRST_NAMES = [
    "Aarav", "Vivaan", "Aditya", "Vihaan", "Arjun", "Sai", "Reyansh", "Ayaan", "Krishna", "Ishaan",
    "Shaurya", "Atharva", "Advik", "Pranav", "Advaith", "Aarush", "Kabir", "Ritvik", "Dhruv", "Harsh",
    "Ananya", "Diya", "Myra", "Sara", "Aadhya", "Isha", "Kiara", "Riya", "Prisha", "Anvi",
    "Aanya", "Navya", "Pari", "Saanvi", "Meera", "Tara", "Zara", "Nisha", "Kavya", "Pooja",
    "Rahul", "Amit", "Vikram", "Rohan", "Karan", "Nikhil", "Siddharth", "Manish", "Deepak", "Suresh",
    "Priya", "Sneha", "Divya", "Swati", "Neha", "Anjali", "Shruti", "Megha", "Pallavi", "Rashmi",
    "James", "John", "Robert", "Michael", "David", "William", "Richard", "Joseph", "Thomas", "Chris",
    "Mary", "Patricia", "Jennifer", "Linda", "Barbara", "Elizabeth", "Susan", "Jessica", "Sarah", "Karen",
    "Alex", "Sam", "Jordan", "Taylor", "Morgan", "Casey", "Jamie", "Quinn", "Riley", "Avery",
    "Emma", "Olivia", "Sophia", "Liam", "Noah", "Ethan", "Mason", "Lucas", "Logan", "Aiden"
]

LAST_NAMES = [
    "Sharma", "Verma", "Gupta", "Singh", "Kumar", "Patel", "Shah", "Mehta", "Joshi", "Rao",
    "Reddy", "Nair", "Iyer", "Menon", "Pillai", "Das", "Bose", "Sen", "Ghosh", "Mukherjee",
    "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez",
    "Wilson", "Anderson", "Thomas", "Taylor", "Moore", "Jackson", "Martin", "Lee", "Perez", "Thompson",
    "White", "Harris", "Clark", "Lewis", "Robinson", "Walker", "Young", "Allen", "King", "Wright"
]

EMAIL_DOMAINS = ["gmail.com", "yahoo.com", "outlook.com", "hotmail.com", "protonmail.com"]

CHANNELS = ["email", "whatsapp"]


def generate_phone():
    return f"+91{random.randint(7000000000, 9999999999)}"


def generate_email(first, last):
    domain = random.choice(EMAIL_DOMAINS)
    sep = random.choice([".", "_", ""])
    num = random.randint(1, 999)
    return f"{first.lower()}{sep}{last.lower()}{num}@{domain}"


def derive_churn_label(recency, frequency, monetary):
    """
    Business rule-based churn labeling:
    - High recency (>180 days) + Low frequency (<5) + Low monetary (<200) → very likely churn
    - High recency (>120 days) + Low frequency (<10) + Low monetary (<500) → likely churn
    - Add some noise for realism
    """
    score = 0
    if recency > 180:
        score += 3
    elif recency > 120:
        score += 2
    elif recency > 60:
        score += 1

    if frequency < 5:
        score += 3
    elif frequency < 10:
        score += 2
    elif frequency < 20:
        score += 1

    if monetary < 200:
        score += 3
    elif monetary < 500:
        score += 2
    elif monetary < 1000:
        score += 1

    # Determine churn probability based on score
    if score >= 7:
        churn_prob = 0.90
    elif score >= 5:
        churn_prob = 0.65
    elif score >= 3:
        churn_prob = 0.30
    else:
        churn_prob = 0.05

    return 1 if random.random() < churn_prob else 0


def generate_dataset(num_customers=500):
    today = datetime(2025, 6, 1)
    customers = []

    for i in range(1, num_customers + 1):
        customer_id = f"C{i:03d}"
        first = random.choice(FIRST_NAMES)
        last = random.choice(LAST_NAMES)
        name = f"{first} {last}"
        email = generate_email(first, last)
        phone = generate_phone()
        age = random.randint(18, 70)
        recency = random.randint(1, 365)
        frequency = random.randint(1, 50)
        monetary = round(random.uniform(10, 5000), 2)
        loyalty_points = random.randint(0, 10000)
        last_purchase_date = (today - timedelta(days=recency)).strftime("%Y-%m-%d")
        signup_days_ago = random.randint(recency + 30, recency + 1500)
        signup_date = (today - timedelta(days=signup_days_ago)).strftime("%Y-%m-%d")
        preferred_channel = random.choice(CHANNELS)
        churn = derive_churn_label(recency, frequency, monetary)

        customers.append({
            "customer_id": customer_id,
            "name": name,
            "email": email,
            "phone": phone,
            "age": age,
            "recency": recency,
            "frequency": frequency,
            "monetary": monetary,
            "loyalty_points": loyalty_points,
            "last_purchase_date": last_purchase_date,
            "signup_date": signup_date,
            "preferred_channel": preferred_channel,
            "churn": churn
        })

    return customers


def save_to_csv(customers, filepath):
    os.makedirs(os.path.dirname(filepath), exist_ok=True)
    fieldnames = [
        "customer_id", "name", "email", "phone", "age",
        "recency", "frequency", "monetary", "loyalty_points",
        "last_purchase_date", "signup_date", "preferred_channel", "churn"
    ]
    with open(filepath, "w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(customers)


if __name__ == "__main__":
    print("=" * 60)
    print("RetainIQ — Synthetic Dataset Generator")
    print("=" * 60)

    customers = generate_dataset(500)

    # Determine output path relative to this script's location
    script_dir = os.path.dirname(os.path.abspath(__file__))
    output_path = os.path.join(script_dir, "customers.csv")

    save_to_csv(customers, output_path)

    # Stats
    churn_count = sum(1 for c in customers if c["churn"] == 1)
    no_churn_count = len(customers) - churn_count

    print(f"\n✅ Generated {len(customers)} customers")
    print(f"   Churned:     {churn_count} ({churn_count / len(customers) * 100:.1f}%)")
    print(f"   Retained:    {no_churn_count} ({no_churn_count / len(customers) * 100:.1f}%)")
    print(f"   Saved to:    {output_path}")
    print("=" * 60)
