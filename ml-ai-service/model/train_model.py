"""
train_model.py — Trains a RandomForestClassifier on the synthetic customer dataset
for churn prediction and saves the model as a joblib file.
"""

import os
import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import accuracy_score, classification_report
from sklearn.preprocessing import StandardScaler
import joblib


def load_data():
    script_dir = os.path.dirname(os.path.abspath(__file__))
    data_path = os.path.join(script_dir, "..", "data", "customers.csv")
    data_path = os.path.normpath(data_path)

    if not os.path.exists(data_path):
        raise FileNotFoundError(
            f"Dataset not found at {data_path}. "
            "Run 'python data/generate_dataset.py' first."
        )

    df = pd.read_csv(data_path)
    print(f"📊 Loaded {len(df)} customers from {data_path}")
    return df


def feature_engineer(df):
    """Create RFM-based features for the model."""
    features = df[["recency", "frequency", "monetary", "age", "loyalty_points"]].copy()

    # RFM Score components (normalized 1-5 scale)
    features["recency_score"] = pd.qcut(df["recency"], q=5, labels=[5, 4, 3, 2, 1]).astype(int)
    features["frequency_score"] = pd.qcut(df["frequency"].rank(method="first"), q=5, labels=[1, 2, 3, 4, 5]).astype(int)
    features["monetary_score"] = pd.qcut(df["monetary"].rank(method="first"), q=5, labels=[1, 2, 3, 4, 5]).astype(int)

    # Composite RFM score
    features["rfm_score"] = (
        features["recency_score"] + features["frequency_score"] + features["monetary_score"]
    )

    # Engagement ratio: frequency relative to account age proxy
    features["engagement_ratio"] = features["frequency"] / (features["recency"] + 1)

    # Monetary per transaction
    features["avg_order_value"] = features["monetary"] / (features["frequency"] + 1)

    # Loyalty density: points relative to monetary
    features["loyalty_density"] = features["loyalty_points"] / (features["monetary"] + 1)

    return features


def train_and_evaluate(df):
    print("\n🔧 Feature engineering...")
    X = feature_engineer(df)
    y = df["churn"]

    feature_names = X.columns.tolist()
    print(f"   Features: {feature_names}")

    # Split
    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.2, random_state=42, stratify=y
    )
    print(f"\n📐 Train: {len(X_train)} | Test: {len(X_test)}")

    # Scale
    scaler = StandardScaler()
    X_train_scaled = scaler.fit_transform(X_train)
    X_test_scaled = scaler.transform(X_test)

    # Train
    print("\n🚀 Training RandomForestClassifier...")
    model = RandomForestClassifier(
        n_estimators=200,
        max_depth=10,
        min_samples_split=5,
        min_samples_leaf=2,
        random_state=42,
        n_jobs=-1
    )
    model.fit(X_train_scaled, y_train)

    # Evaluate
    y_pred = model.predict(X_test_scaled)
    accuracy = accuracy_score(y_test, y_pred)

    print(f"\n{'=' * 60}")
    print(f"📈 Model Accuracy: {accuracy:.4f} ({accuracy * 100:.2f}%)")
    print(f"{'=' * 60}")
    print("\n📋 Classification Report:")
    print(classification_report(y_test, y_pred, target_names=["Retained", "Churned"]))

    # Feature importance
    importance = pd.Series(model.feature_importances_, index=feature_names)
    importance = importance.sort_values(ascending=False)
    print("🎯 Feature Importance:")
    for feat, imp in importance.items():
        bar = "█" * int(imp * 50)
        print(f"   {feat:25s} {imp:.4f} {bar}")

    return model, scaler, feature_names


def save_model(model, scaler, feature_names):
    script_dir = os.path.dirname(os.path.abspath(__file__))
    os.makedirs(script_dir, exist_ok=True)

    model_path = os.path.join(script_dir, "churn_model.joblib")
    artifact = {
        "model": model,
        "scaler": scaler,
        "feature_names": feature_names
    }
    joblib.dump(artifact, model_path)
    print(f"\n💾 Model saved to: {model_path}")
    return model_path


if __name__ == "__main__":
    print("=" * 60)
    print("RetainIQ — Churn Model Training Pipeline")
    print("=" * 60)

    df = load_data()
    model, scaler, feature_names = train_and_evaluate(df)
    save_model(model, scaler, feature_names)

    print("\n✅ Training complete!")
    print("=" * 60)
