"use client";

import { useState, useEffect, useCallback } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { apiClient } from "@/lib/api-client";
import { useAuthStore } from "@/stores/auth-store";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Phone, Lock, Eye, EyeOff } from "lucide-react";
import { cn } from "@/lib/utils";

// Google OAuth Client ID from environment
const GOOGLE_CLIENT_ID = process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID || "884521630755-bd3rvn1a64pcsot27sjr2a2unj46boad.apps.googleusercontent.com";

declare global {
  interface Window {
    google?: {
      accounts?: {
        id?: {
          initialize: (config: any) => void;
          prompt: (callback?: (notification: any) => void) => void;
          renderButton: (element: HTMLElement, config: any) => void;
        };
      };
    };
  }
}

export default function LoginPage() {
  const router = useRouter();
  const setAuth = useAuthStore((s) => s.setAuth);
  const [phone, setPhone] = useState("");
  const [password, setPassword] = useState("");
  const [showPw, setShowPw] = useState(false);
  const [loading, setLoading] = useState(false);
  const [googleLoading, setGoogleLoading] = useState(false);
  const [error, setError] = useState("");

  // Handle Google credential response
  const handleGoogleCredential = useCallback(async (response: any) => {
    setGoogleLoading(true);
    setError("");
    try {
      const { data } = await apiClient.post("/api/auth/google", {
        credential: response.credential,
        clientId: response.clientId || GOOGLE_CLIENT_ID,
      });
      if (!data.success) {
        setError(data.message || "Google login failed.");
        setGoogleLoading(false);
        return;
      }
      const store = data.store || {};
      setAuth(data.token, store, {
        name: store.owner_name || store.store_name || "Google User",
        phone: store.phone || "",
        email: store.email || "",
      });
      router.replace("/dashboard");
    } catch (err: any) {
      setError(
        err?.response?.data?.message ||
          "Google login failed. Please try again."
      );
      setGoogleLoading(false);
    }
  }, [setAuth, router]);

  // Initialize Google Sign-In
  useEffect(() => {
    if (!GOOGLE_CLIENT_ID) return;

    // Load Google Identity Services script
    const script = document.createElement("script");
    script.src = "https://accounts.google.com/gsi/client";
    script.async = true;
    script.defer = true;
    script.onload = () => {
      if (window.google?.accounts?.id) {
        window.google.accounts.id.initialize({
          client_id: GOOGLE_CLIENT_ID,
          callback: handleGoogleCredential,
          auto_select: false,
          cancel_on_tap_outside: true,
        });

        // Render the Google Sign-In button
        const btnContainer = document.getElementById("google-signin-btn");
        if (btnContainer) {
          window.google.accounts.id.renderButton(btnContainer, {
            type: "standard",
            theme: "outline",
            size: "large",
            text: "continue_with",
            shape: "rectangular",
            width: btnContainer.offsetWidth || 300,
          });
        }
      }
    };
    document.head.appendChild(script);

    return () => {
      // Cleanup
      const existingScript = document.querySelector(
        'script[src="https://accounts.google.com/gsi/client"]'
      );
      if (existingScript) existingScript.remove();
    };
  }, [handleGoogleCredential]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError("");
    try {
      const { data } = await apiClient.post("/api/auth/login", {
        phone: phone.replace(/\D/g, ""),
        password,
      });
      if (!data.success || !data.token) {
        setError(data.message || "Login failed. Please check your credentials.");
        setLoading(false);
        return;
      }
      const store = data.store || {};
      setAuth(data.token, store, {
        name: store.owner_name || store.store_name || "Store Owner",
        phone: phone,
        email: store.email || "",
      });
      router.replace("/dashboard");
    } catch (err: any) {
      setError(
        err?.response?.data?.message ||
          "Could not connect to server. Please try again."
      );
      setLoading(false);
    }
  };

  return (
    <div className="flex min-h-[100dvh] flex-col items-center justify-center px-4 py-12 bg-gradient-to-b from-primary/5 to-transparent">
      {/* Logo */}
      <Link href="/" className="mb-8 flex items-center gap-3">
        <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-primary shadow-lg shadow-primary/30">
          <span className="text-2xl">🏪</span>
        </div>
        <span className="text-2xl font-extrabold tracking-tight text-foreground">
          Grahbook
        </span>
      </Link>

      {/* Card */}
      <div className="w-full max-w-sm rounded-2xl border border-border bg-white dark:bg-dark-800/70 p-6 shadow-2xl backdrop-blur-xl">
        <div className="mb-6 text-center">
          <h1 className="text-xl font-bold text-foreground">Welcome back</h1>
          <p className="mt-1 text-sm text-muted-foreground">
            Sign in to manage your store
          </p>
        </div>

        {/* Google Sign-In Button */}
        {GOOGLE_CLIENT_ID && (
          <>
            <div id="google-signin-btn" className="w-full mb-4" />
            <div className="relative mb-4">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-border" />
              </div>
              <div className="relative flex justify-center text-xs uppercase">
                <span className="bg-white dark:bg-dark-800 px-2 text-muted-foreground">
                  or sign in with phone
                </span>
              </div>
            </div>
          </>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          {/* Phone */}
          <div className="space-y-1.5">
            <label className="text-sm font-medium text-foreground">
              Phone Number
            </label>
            <div className="relative">
              <Phone className="absolute left-3.5 top-3.5 h-4 w-4 text-muted-foreground" />
              <Input
                type="tel"
                placeholder="9876543210"
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
                className="pl-10 h-12"
                required
                pattern="[6-9]{1}[0-9]{9}"
                autoComplete="tel-national"
              />
            </div>
          </div>

          {/* Password */}
          <div className="space-y-1.5">
            <label className="text-sm font-medium text-foreground">Password</label>
            <div className="relative">
              <Lock className="absolute left-3.5 top-3.5 h-4 w-4 text-muted-foreground" />
              <Input
                type={showPw ? "text" : "password"}
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="pl-10 pr-10 h-12"
                required
              />
              <button
                type="button"
                onClick={() => setShowPw(!showPw)}
                className="absolute right-3 top-3.5 text-muted-foreground hover:text-foreground"
              >
                {showPw ? (
                  <EyeOff className="h-4 w-4" />
                ) : (
                  <Eye className="h-4 w-4" />
                )}
              </button>
            </div>
          </div>

          {/* Error */}
          {error && (
            <p className="text-sm text-red-500 bg-red-50 dark:bg-red-900/20 px-3 py-2 rounded-lg">
              {error}
            </p>
          )}

          {/* Submit */}
          <Button
            type="submit"
            className="w-full h-12 text-base font-bold"
            disabled={loading}
          >
            {loading ? (
              <span className="flex items-center gap-2">
                <span className="h-4 w-4 rounded-full border-2 border-white/30 border-t-white animate-spin" />
                Signing in...
              </span>
            ) : (
              "Sign In"
            )}
          </Button>
        </form>

        <p className="mt-5 text-center text-sm text-muted-foreground">
          Don&apos;t have an account?{" "}
          <Link
            href="/register"
            className="font-semibold text-primary hover:underline"
          >
            Register
          </Link>
        </p>
      </div>

      <p className="mt-8 text-xs text-muted-foreground text-center">
        By signing in you agree to our Terms & Privacy Policy
      </p>
    </div>
  );
}
