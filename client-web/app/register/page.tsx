"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { apiClient } from "@/lib/api-client";
import { useAuthStore } from "@/stores/auth-store";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Phone, Lock, Eye, EyeOff, User, Mail, Store } from "lucide-react";

export default function RegisterPage() {
  const router = useRouter();
  const setAuth = useAuthStore((s) => s.setAuth);
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [phone, setPhone] = useState("");
  const [password, setPassword] = useState("");
  const [showPw, setShowPw] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError("");
    try {
      // Step 1: Register store
      const { data: regData } = await apiClient.post("/api/register-store", {
        store_name: `${name}'s Store`,
        owner_name: name,
        phone,
        email,
        business_type: "retail",
        plan: "basic",
        password,
      });

      if (regData.status === "error") {
        setError(regData.message || "Registration failed");
        setLoading(false);
        return;
      }

      // Step 2: Auto-login
      try {
        const { data: loginData } = await apiClient.post("/api/auth/login", {
          phone,
          password,
        });
        if (loginData.success && loginData.token) {
          const store = loginData.store || {};
          setAuth(loginData.token, store, {
            name,
            phone,
            email,
          });
          router.replace("/dashboard");
          return;
        }
      } catch {}

      // If auto-login fails, go to login screen
      router.replace("/login");
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
      <Link href="/" className="mb-8 flex items-center gap-3">
        <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-primary shadow-lg shadow-primary/30">
          <span className="text-2xl">🏪</span>
        </div>
        <span className="text-2xl font-extrabold tracking-tight text-foreground">
          Grahbook
        </span>
      </Link>

      <div className="w-full max-w-sm rounded-2xl border border-border bg-white dark:bg-dark-800/70 p-6 shadow-2xl backdrop-blur-xl">
        <div className="mb-6 text-center">
          <h1 className="text-xl font-bold text-foreground">Create account</h1>
          <p className="mt-1 text-sm text-muted-foreground">
            Start managing your store on WhatsApp
          </p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-3.5">
          <div className="space-y-1.5">
            <label className="text-sm font-medium text-foreground">Full Name</label>
            <div className="relative">
              <User className="absolute left-3.5 top-3.5 h-4 w-4 text-muted-foreground" />
              <Input
                type="text"
                placeholder="Rajesh Sharma"
                value={name}
                onChange={(e) => setName(e.target.value)}
                className="pl-10 h-12"
                required
              />
            </div>
          </div>

          <div className="space-y-1.5">
            <label className="text-sm font-medium text-foreground">Email</label>
            <div className="relative">
              <Mail className="absolute left-3.5 top-3.5 h-4 w-4 text-muted-foreground" />
              <Input
                type="email"
                placeholder="your@store.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="pl-10 h-12"
              />
            </div>
          </div>

          <div className="space-y-1.5">
            <label className="text-sm font-medium text-foreground">Phone Number</label>
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
              />
            </div>
          </div>

          <div className="space-y-1.5">
            <label className="text-sm font-medium text-foreground">Password</label>
            <div className="relative">
              <Lock className="absolute left-3.5 top-3.5 h-4 w-4 text-muted-foreground" />
              <Input
                type={showPw ? "text" : "password"}
                placeholder="Min. 6 characters"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="pl-10 pr-10 h-12"
                required
                minLength={6}
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

          {error && (
            <p className="text-sm text-red-500 bg-red-50 dark:bg-red-900/20 px-3 py-2 rounded-lg">
              {error}
            </p>
          )}

          <Button
            type="submit"
            className="w-full h-12 text-base font-bold"
            disabled={loading}
          >
            {loading ? (
              <span className="flex items-center gap-2">
                <span className="h-4 w-4 rounded-full border-2 border-white/30 border-t-white animate-spin" />
                Creating...
              </span>
            ) : (
              "Create Account"
            )}
          </Button>
        </form>

        <p className="mt-5 text-center text-sm text-muted-foreground">
          Already have an account?{" "}
          <Link
            href="/login"
            className="font-semibold text-primary hover:underline"
          >
            Sign in
          </Link>
        </p>
      </div>

      <p className="mt-8 text-xs text-muted-foreground text-center max-w-xs">
        By registering you agree to our Terms & Privacy Policy. Your data is
        stored securely.
      </p>
    </div>
  );
}
