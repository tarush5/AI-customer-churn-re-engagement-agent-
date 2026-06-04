"""
agent.py — LangChain Agent for RetainIQ campaign generation.
Uses Groq (llama-3.3-70b-versatile) by default with OpenAI fallback.
Includes a template-based fallback when no API key is available.
"""

import os
import re
import json
from dotenv import load_dotenv

load_dotenv()

# --- Fallback Template Engine (no LLM needed) ---

FALLBACK_TEMPLATES = {
    "high": {
        "campaign_type": "Win-Back Offer",
        "subject": "We miss you, {name}! Here's 25% OFF just for you 💸",
        "message_email": (
            "Hi {name},\n\n"
            "We noticed it's been a while since your last visit — {recency} days to be exact! "
            "We truly value you as a customer, and we'd love to welcome you back.\n\n"
            "As a special gesture, here's an EXCLUSIVE 25% DISCOUNT on your next purchase. "
            "Use code: COMEBACK25 at checkout.\n\n"
            "This offer expires in 48 hours — don't let it slip away!\n\n"
            "🛍️ Shop Now and Save Big\n\n"
            "Warm regards,\nThe RetainIQ Team"
        ),
        "message_whatsapp": (
            "Hey {name}! 👋\n\n"
            "It's been {recency} days since we last saw you, and we miss you! 😢\n\n"
            "Here's something special — *25% OFF* your next order! 🎉\n"
            "Use code: *COMEBACK25*\n\n"
            "⏰ Hurry, this expires in 48 hours!\n"
            "Tap here to shop → [link]"
        ),
        "strategy_reasoning": (
            "Customer shows HIGH churn risk (score: {churn_score}/100). "
            "Recency of {recency} days with only {frequency} purchases totaling ${monetary}. "
            "Aggressive win-back strategy with 25% discount deployed to maximize re-engagement probability. "
            "Urgency tactics (48hr expiry) used to drive immediate action."
        )
    },
    "medium": {
        "campaign_type": "Loyalty Boost",
        "subject": "{name}, you've earned DOUBLE loyalty points this week! ⭐",
        "message_email": (
            "Hi {name},\n\n"
            "Great news! As one of our valued customers, you've been selected for our "
            "Double Points Week promotion! 🌟\n\n"
            "For the next 7 days, every purchase earns you 2X loyalty points. "
            "That means faster rewards, bigger savings, and more perks just for you.\n\n"
            "With your current {loyalty_points} points, you're already on your way to "
            "amazing rewards. Let's double down! 🚀\n\n"
            "🛍️ Start Earning Double Points Now\n\n"
            "Best,\nThe RetainIQ Team"
        ),
        "message_whatsapp": (
            "Hi {name}! 🌟\n\n"
            "Exciting news — you've been selected for *Double Points Week*! 🎯\n\n"
            "Every purchase this week = *2X loyalty points*\n"
            "Your current balance: *{loyalty_points} points*\n\n"
            "Start earning more → [link]"
        ),
        "strategy_reasoning": (
            "Customer shows MEDIUM churn risk (score: {churn_score}/100). "
            "Moderate engagement with {frequency} purchases over {recency} days. "
            "Loyalty points boost strategy chosen to reinforce existing relationship "
            "and incentivize continued engagement without heavy discounting."
        )
    },
    "low": {
        "campaign_type": "Engagement & New Arrivals",
        "subject": "🔥 {name}, check out what's new — curated just for you!",
        "message_email": (
            "Hi {name},\n\n"
            "We've got some exciting new arrivals that we think you'll love! "
            "Based on your amazing taste (you've shopped with us {frequency} times!), "
            "here are some hand-picked recommendations:\n\n"
            "🆕 New Collection Drop — Fresh styles just landed\n"
            "⚡ Trending Now — See what everyone's talking about\n"
            "💎 Exclusive Preview — Be the first to shop\n\n"
            "Plus, some of these are selling fast — don't miss out!\n\n"
            "🛍️ Explore New Arrivals\n\n"
            "Cheers,\nThe RetainIQ Team"
        ),
        "message_whatsapp": (
            "Hey {name}! 🔥\n\n"
            "New arrivals alert! We picked some items just for you 😍\n\n"
            "🆕 Fresh collection just dropped\n"
            "⚡ Trending items selling fast\n"
            "💎 Exclusive early access for you!\n\n"
            "Check them out → [link]"
        ),
        "strategy_reasoning": (
            "Customer shows LOW churn risk (score: {churn_score}/100). "
            "Active customer with {frequency} purchases. "
            "FOMO-driven engagement strategy with new arrivals and exclusivity "
            "to maintain high activity levels and increase purchase frequency."
        )
    }
}


def fallback_generate_campaign(customer_data: dict) -> dict:
    """Generate a campaign using templates when no LLM is available."""
    risk_level = customer_data.get("risk_level", "medium").lower()
    channel = customer_data.get("preferred_channel", "email").lower()
    template = FALLBACK_TEMPLATES.get(risk_level, FALLBACK_TEMPLATES["medium"])

    format_data = {
        "name": customer_data.get("customer_name", "Valued Customer"),
        "recency": customer_data.get("recency", 0),
        "frequency": customer_data.get("frequency", 0),
        "monetary": customer_data.get("monetary", 0),
        "churn_score": customer_data.get("churn_score", 50),
        "loyalty_points": customer_data.get("loyalty_points", 0),
    }

    message_key = "message_whatsapp" if channel == "whatsapp" else "message_email"

    return {
        "campaign_type": template["campaign_type"],
        "subject": template["subject"].format(**format_data),
        "message": template[message_key].format(**format_data),
        "channel": channel,
        "strategy_reasoning": template["strategy_reasoning"].format(**format_data),
    }


# --- LangChain Agent (LLM-powered) ---

def _get_llm():
    """Initialize the LLM based on environment configuration."""
    provider = os.getenv("LLM_PROVIDER", "groq").lower()

    if provider == "groq":
        api_key = os.getenv("GROQ_API_KEY", "")
        if api_key and api_key != "your_key_here":
            try:
                from langchain_groq import ChatGroq
                return ChatGroq(
                    model="llama-3.3-70b-versatile",
                    temperature=0.7,
                    api_key=api_key,
                )
            except Exception as e:
                print(f"⚠️  Groq init failed: {e}")

    if provider == "openai" or provider == "groq":
        api_key = os.getenv("OPENAI_API_KEY", "")
        if api_key and api_key != "your_key_here":
            try:
                from langchain_openai import ChatOpenAI
                return ChatOpenAI(
                    model="gpt-4o-mini",
                    temperature=0.7,
                    api_key=api_key,
                )
            except Exception as e:
                print(f"⚠️  OpenAI init failed: {e}")

    return None


def _build_agent_prompt(customer_data: dict) -> str:
    """Build the prompt for the LangChain agent."""
    return f"""You are RetainIQ, an AI retention specialist for retail brands. 
You analyze customer behavior and craft compelling, personalized re-engagement messages.

## Customer Profile
- **Name**: {customer_data.get('customer_name', 'Customer')}
- **Email**: {customer_data.get('email', 'N/A')}
- **Phone**: {customer_data.get('phone', 'N/A')}
- **Recency**: {customer_data.get('recency', 0)} days since last purchase
- **Frequency**: {customer_data.get('frequency', 0)} total purchases
- **Monetary**: ${customer_data.get('monetary', 0)} total spend
- **Churn Score**: {customer_data.get('churn_score', 50)}/100
- **Risk Level**: {customer_data.get('risk_level', 'medium')}
- **Preferred Channel**: {customer_data.get('preferred_channel', 'email')}

## Your Tasks
1. **Analyze Churn Risk**: Based on the churn score of {customer_data.get('churn_score', 50)}/100 and risk level "{customer_data.get('risk_level', 'medium')}":
   - High risk (score > 70): Customer is very likely to churn. Needs aggressive win-back with 20-30% discount.
   - Medium risk (score 40-70): Customer needs re-engagement. Use loyalty points boost strategy.
   - Low risk (score < 40): Customer is active. Use FOMO, new arrivals, and engagement tactics.

2. **Determine Campaign Strategy**: Choose the right approach based on risk level.

3. **Generate Personalized Message**: Create a compelling {customer_data.get('preferred_channel', 'email')} message for {customer_data.get('customer_name', 'the customer')}.

## Output Format
Respond with ONLY a valid JSON object (no markdown, no code blocks) with these exact keys:
{{
    "campaign_type": "the campaign type (e.g., Win-Back Offer, Loyalty Boost, Engagement & New Arrivals)",
    "subject": "compelling subject line",
    "message": "the full personalized message body for {customer_data.get('preferred_channel', 'email')}",
    "channel": "{customer_data.get('preferred_channel', 'email')}",
    "strategy_reasoning": "brief explanation of why this strategy was chosen based on the customer data"
}}"""


def _parse_llm_response(response_text: str) -> dict:
    """Parse the LLM response into a structured dict."""
    # Try direct JSON parse
    try:
        return json.loads(response_text)
    except json.JSONDecodeError:
        pass

    # Try extracting JSON from markdown code blocks
    json_match = re.search(r'```(?:json)?\s*(\{.*?\})\s*```', response_text, re.DOTALL)
    if json_match:
        try:
            return json.loads(json_match.group(1))
        except json.JSONDecodeError:
            pass

    # Try finding any JSON object in the text
    json_match = re.search(r'\{[^{}]*(?:\{[^{}]*\}[^{}]*)*\}', response_text, re.DOTALL)
    if json_match:
        try:
            return json.loads(json_match.group(0))
        except json.JSONDecodeError:
            pass

    # Return raw text as message if all parsing fails
    return {
        "campaign_type": "Personalized Campaign",
        "subject": "A special message for you",
        "message": response_text,
        "channel": "email",
        "strategy_reasoning": "Generated by AI agent"
    }


async def generate_campaign(customer_data: dict) -> dict:
    """
    Main entry point for campaign generation.
    Tries LLM-powered generation first, falls back to templates.
    """
    llm = _get_llm()

    if llm is None:
        print("ℹ️  No LLM available — using template-based fallback")
        result = fallback_generate_campaign(customer_data)
        result["generation_mode"] = "fallback_template"
        return result

    try:
        prompt = _build_agent_prompt(customer_data)
        response = llm.invoke(prompt)
        response_text = response.content if hasattr(response, "content") else str(response)
        result = _parse_llm_response(response_text)
        result["generation_mode"] = "llm_powered"
        return result

    except Exception as e:
        print(f"⚠️  LLM generation failed: {e}")
        print("ℹ️  Falling back to template-based generation")
        result = fallback_generate_campaign(customer_data)
        result["generation_mode"] = "fallback_template"
        return result
