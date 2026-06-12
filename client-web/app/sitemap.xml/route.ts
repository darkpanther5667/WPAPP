import { MetadataRoute } from "next";
import { SITE_URL } from "@/lib/constants";

export default function sitemap(): MetadataRoute.Sitemap {
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

  return {
    // Standard sitemap items for static pages
    ...staticPages.reduce(
      (acc, path) => {
        acc.push({
          url: `${SITE_URL}${path}`,
          lastModified: new Date(),
          changeFrequency: path === "" ? "daily" : "monthly",
          priority: path === "" ? 1.0 : 0.5,
        });
        return acc;
      },
      [] as MetadataRoute.Sitemap
    ),
  };
}
