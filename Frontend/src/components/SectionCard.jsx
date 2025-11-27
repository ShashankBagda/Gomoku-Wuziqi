import React from "react";

export default function SectionCard({ title, children, className = "" }) {
  return (
    <div className={`border-2 border-gray-300 dark:border-gray-600 rounded-md p-6 w-full max-w-3xl ${className}`}>
      <h2 className="text-xl font-semibold mb-4 text-center">{title}</h2>
      {children}
    </div>
  );
}
