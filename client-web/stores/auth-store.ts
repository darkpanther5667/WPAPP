"use client";

import { create } from "zustand";
import { persist, createJSONStorage } from "zustand/middleware";
import type { Store } from "@/types";

export interface AuthState {
  token: string | null;
  store: Store | null;
  user: { name: string; phone: string; email?: string } | null;
  isAuthenticated: boolean;
  setAuth: (token: string, store: Store, user: { name: string; phone: string; email?: string }) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      token: null,
      store: null,
      user: null,
      isAuthenticated: false,
      setAuth: (token, store, user) =>
        set({ token, store, user, isAuthenticated: true }),
      logout: () =>
        set({ token: null, store: null, user: null, isAuthenticated: false }),
    }),
    {
      name: "gh_user",
      storage: createJSONStorage(() => localStorage),
    }
  )
);
