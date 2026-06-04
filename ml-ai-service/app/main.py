"""
main.py — FastAPI application for RetainIQ ML & AI service.
Provides churn prediction and AI-powered campaign generation endpoints.
"""

import os
import sys
from contextlib import asynccontextmanager

import numpy as np
import pandas as pd
import joblib
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from dotenv import load_dotenv

# Load environment variables
load_dotenv()

# Add project root to path
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from app.agent import generate_campaign


# --- Pydantic Models ---

class PredictRequest(BaseModel):
    recency: float = Field(..., description="Days since last purchase", ge=0)
    frequency: float = Field(..., description="Number of purchases", ge=0)
    monetary: float = Field(..., description="Total spend amount", ge=0)
    age: float = Field(..., description="Customer age", ge=0)
    loyalty_points: float = Field(..., description="Loyalty points balance", ge=0)


class PredictResponse(BaseModel):
    churn_score: float = Field(..., description="Churn probability score 0-100")
    risk_level: str = Field(..., description="Risk level: high, medium, or low")


class CampaignRequest(BaseModel):
    customer_name: str = Field(..., description="Customer's full name")
    email: str = Field(..., description="Customer email address")
    phone: str = Field(..., description="Customer phone number")
    recency: float = Field(..., description="Days since last purchase")
    frequency: float = Field(..., description="Number of purchases")
    monetary: float = Field(..., description="Total spend amount")
    churn_score: float = Field(..., description="Churn score 0-100")
    risk_level: str = Field(..., description="Risk level: high, medium, or low")
    preferred_channel: str = Field(default="email", description="Preferred channel: email or whatsapp")


class CampaignResponse(BaseModel):
    campaign_type: str
    subject: str
    message: str
    channel: str
    strategy_reasoning: str
    generation_mode: str = Field(default="fallback_template", description="How the campaign was generated")


# --- Model Loading ---

model_artifact = None


def load_model():
    """Load the trained churn model and preprocessing artifacts."""
    global model_artifact
    model_path = os.path.join(
        os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
        "model", "churn_model.joblib"
    )

    if not os.path.exists(model_path):
        print(f"⚠️  Model not found at {model_path}")
        print("   Run 'python model/train_model.py' to train the model first.")
        return False

    model_artifact = joblib.load(model_path)
    print(f"✅ Model loaded from {model_path}")
    return True


def predict_churn(recency, frequency, monetary, age, loyalty_points):
    """Run churn prediction using the loaded model."""
    if model_artifact is None:
        raise RuntimeError("Model not loaded")

    model = model_artifact["model"]
    scaler = model_artifact["scaler"]
    feature_names = model_artifact["feature_names"]

    # Recreate the same features used during training
    raw = pd.DataFrame([{
        "recency": recency,
        "frequency": frequency,
        "monetary": monetary,
        "age": age,
        "loyalty_points": loyalty_points,
    }])

    # RFM scores (approximate using thresholds since we can't use qcut on single row)
    recency_score = 5 if recency < 30 else 4 if recency < 90 else 3 if recency < 180 else 2 if recency < 270 else 1
    frequency_score = 1 if frequency < 5 else 2 if frequency < 15 else 3 if frequency < 25 else 4 if frequency < 35 else 5
    monetary_score = 1 if monetary < 500 else 2 if monetary < 1500 else 3 if monetary < 2500 else 4 if monetary < 3500 else 5

    features = pd.DataFrame([{
        "recency": recency,
        "frequency": frequency,
        "monetary": monetary,
        "age": age,
        "loyalty_points": loyalty_points,
        "recency_score": recency_score,
        "frequency_score": frequency_score,
        "monetary_score": monetary_score,
        "rfm_score": recency_score + frequency_score + monetary_score,
        "engagement_ratio": frequency / (recency + 1),
        "avg_order_value": monetary / (frequency + 1),
        "loyalty_density": loyalty_points / (monetary + 1),
    }])

    # Ensure column order matches training
    features = features[feature_names]

    # Scale and predict
    features_scaled = scaler.transform(features)
    proba = model.predict_proba(features_scaled)[0]
    churn_probability = proba[1]  # Probability of class 1 (churn)

    churn_score = round(churn_probability * 100, 2)
    risk_level = "high" if churn_score > 70 else "medium" if churn_score > 40 else "low"

    return churn_score, risk_level


# --- App Lifecycle ---

@asynccontextmanager
async def lifespan(app: FastAPI):
    print("\n" + "=" * 60)
    print("🚀 RetainIQ ML & AI Service — Starting Up")
    print("=" * 60)
    success = load_model()
    if success:
        print("✅ All systems ready!")
    else:
        print("⚠️  Running without model — /predict will return errors")
    print("=" * 60 + "\n")
    yield
    print("\n👋 RetainIQ shutting down...")


# --- FastAPI App ---

app = FastAPI(
    title="RetainIQ ML & AI Service",
    description=(
        "AI-powered customer retention platform. "
        "Predicts churn risk and generates personalized re-engagement campaigns."
    ),
    version="1.0.0",
    lifespan=lifespan,
)

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# --- Endpoints ---

@app.get("/health")
async def health_check():
    """Health check endpoint."""
    return {
        "status": "healthy",
        "service": "RetainIQ ML & AI Service",
        "model_loaded": model_artifact is not None,
        "version": "1.0.0",
    }


@app.post("/predict", response_model=PredictResponse)
async def predict(request: PredictRequest):
    """Predict churn risk for a customer based on their RFM and demographic data."""
    if model_artifact is None:
        raise HTTPException(
            status_code=503,
            detail="Model not loaded. Run 'python model/train_model.py' first."
        )

    try:
        churn_score, risk_level = predict_churn(
            recency=request.recency,
            frequency=request.frequency,
            monetary=request.monetary,
            age=request.age,
            loyalty_points=request.loyalty_points,
        )
        return PredictResponse(churn_score=churn_score, risk_level=risk_level)

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Prediction failed: {str(e)}")


@app.post("/generate-campaign", response_model=CampaignResponse)
async def generate_campaign_endpoint(request: CampaignRequest):
    """Generate a personalized re-engagement campaign for a customer."""
    try:
        customer_data = {
            "customer_name": request.customer_name,
            "email": request.email,
            "phone": request.phone,
            "recency": request.recency,
            "frequency": request.frequency,
            "monetary": request.monetary,
            "churn_score": request.churn_score,
            "risk_level": request.risk_level,
            "preferred_channel": request.preferred_channel,
        }

        result = await generate_campaign(customer_data)

        return CampaignResponse(
            campaign_type=result.get("campaign_type", "General Campaign"),
            subject=result.get("subject", "We miss you!"),
            message=result.get("message", ""),
            channel=result.get("channel", request.preferred_channel),
            strategy_reasoning=result.get("strategy_reasoning", ""),
            generation_mode=result.get("generation_mode", "unknown"),
        )

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Campaign generation failed: {str(e)}")


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=True)
