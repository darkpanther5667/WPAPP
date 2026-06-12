import { SITE_URL } from "@/lib/constants";

const staticPages = [
  "",
  "/about",
  "/contact",
  "/privacy-policy",
  "/terms-and-conditions",
  "/cancellation-policy",
  "/return-refund-policy",
  "/shipping-policy",
  "/login",
  "/register",
];

export function GET() {
  const urlEntries = staticPages
    .map((route) => {
      const changeFrequency = route === "" ? "daily" : "monthly";
      const priority = route === "" ? "1.0" : "0.5";
      return `  <url>
    <loc>${SITE_URL}${route}</loc>
    <lastmod>${new Date().toISOString()}</lastmod>
    <changefreq>${changeFrequency}</changefreq>
    <priority>${priority}</priority>
  </url>`;
    })
    .join("\n");

  const xml = `<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
${urlEntries}
</urlset>`;

  return new Response(xml, {
    headers: {
      "Content-Type": "application/xml; charset=utf-8",
    },
  });
}
