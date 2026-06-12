import type { Metadata, Viewport } from "next";
import { Poppins, JetBrains_Mono } from "next/font/google";
import { ThemeProvider } from "@/components/theme-provider";
import { AuthGuard } from "@/components/auth-guard";
import { AppShell } from "@/components/app-shell";
import { Toaster } from "@/components/ui/toast";
import { ErrorBoundary } from "@/components/error-boundary";
import { SITE_URL, APP_NAME, APP_DESCRIPTION } from "@/lib/constants";
import "./globals.css";

const poppins = Poppins({
  subsets: ["latin"],
  weight: ["400", "500", "600", "700", "800"],
  variable: "--font-sans",
});

const jetbrainsMono = JetBrains_Mono({
  subsets: ["latin"],
  weight: ["400", "500", "600", "700"],
  variable: "--font-mono",
});

export const viewport: Viewport = {
  themeColor: "#25d366",
  width: "device-width",
  initialScale: 1,
};

export const metadata: Metadata = {
  metadataBase: new URL(SITE_URL),
  title: {
    default: `${APP_NAME} — AI WhatsApp Store Bot for Indian Merchants`,
    template: `%s | ${APP_NAME}`,
  },
  description: APP_DESCRIPTION,
  applicationName: APP_NAME,
  authors: [{ name: "Grahbook Team" }],
  generator: "Next.js",
  keywords: [
    "WhatsApp billing",
    "AI store bot",
    "Indian merchant",
    "UPI payments",
    "invoice app",
    "GST billing",
    "customer management",
    "small business India",
    "billing software",
    "WhatsApp business",
  ],
  referrer: "origin-when-cross-origin",
  robots: {
    index: true,
    follow: true,
    googleBot: {
      index: true,
      follow: true,
      "max-video-preview": -1,
      "max-image-preview": "large",
      "max-snippet": -1,
    },
  },
  openGraph: {
    title: `${APP_NAME} — AI WhatsApp Store Bot for Indian Merchants`,
    description: APP_DESCRIPTION,
    url: SITE_URL,
    siteName: APP_NAME,
    images: [
      {
        url: `${SITE_URL}/og-image.png`,
        width: 1200,
        height: 630,
        alt: `${APP_NAME} — AI-powered billing & customer management on WhatsApp`,
      },
    ],
    type: "website",
    locale: "en_IN",
  },
  twitter: {
    card: "summary_large_image",
    title: `${APP_NAME} — AI WhatsApp Store Bot`,
    description: APP_DESCRIPTION,
    images: [`${SITE_URL}/og-image.png`],
    creator: "@grahbook",
  },
  alternates: {
    canonical: SITE_URL,
  },
};

const SITE_JSONLD = {
  "@context": "https://schema.org",
  "@type": "SoftwareApplication",
  name: APP_NAME,
  applicationCategory: "BusinessApplication",
  operatingSystem: "Web, Android",
  description: APP_DESCRIPTION,
  url: SITE_URL,
  offers: [
    {
      "@type": "Offer",
      name: "Mukhiya",
      price: "0",
      priceCurrency: "INR",
      description: "Free plan — 1,000 orders/month",
    },
    {
      "@type": "Offer",
      name: "Bhandari",
      price: "149",
      priceCurrency: "INR",
      description: "Pro plan — unlimited orders, GST billing, Tally export",
    },
    {
      "@type": "Offer",
      name: "Samrat",
      price: "399",
      priceCurrency: "INR",
      description: "Enterprise — unlimited everything, dedicated support",
    },
  ],
  aggregateRating: {
    "@type": "AggregateRating",
    ratingValue: "4.8",
    ratingCount: "1200",
    bestRating: "5",
  },
  publisher: {
    "@type": "Organization",
    name: APP_NAME,
    url: SITE_URL,
  },
};

const WEBSITE_JSONLD = {
  "@context": "https://schema.org",
  "@type": "WebSite",
  name: APP_NAME,
  url: SITE_URL,
  description: APP_DESCRIPTION,
  inLanguage: "en-IN",
  potentialAction: {
    "@type": "SearchAction",
    target: `${SITE_URL}/search?q={search_term_string}`,
    "query-input": "required name=search_term_string",
  },
};

const ORGANIZATION_JSONLD = {
  "@context": "https://schema.org",
  "@type": "LocalBusiness",
  name: APP_NAME,
  url: SITE_URL,
  description: APP_DESCRIPTION,
  foundingDate: "2024",
  priceRange: "₹",
  areaServed: {
    "@type": "Country",
    name: "India",
  },
};

function JsonLd() {
  const items = [SITE_JSONLD, WEBSITE_JSONLD, ORGANIZATION_JSONLD];
  return (
    <>
      {items.map((item, i) => (
        <script
          key={i}
          type="application/ld+json"
          dangerouslySetInnerHTML={{ __html: JSON.stringify(item) }}
        />
      ))}
    </>
  );
}

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" suppressHydrationWarning>
      <body
        className={`min-h-screen antialiased ${poppins.variable} ${jetbrainsMono.variable}`}
      >
        <JsonLd />
        <ThemeProvider
          attribute="class"
          defaultTheme="system"
          enableSystem
          disableTransitionOnChange
        >
          <AuthGuard />
          <ErrorBoundary>
            <AppShell>{children}</AppShell>
          </ErrorBoundary>
          <Toaster />
        </ThemeProvider>
      </body>
    </html>
  );
}
