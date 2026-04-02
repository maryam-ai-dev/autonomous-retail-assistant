"use client";

interface PolicyWarningsPanelProps {
  warnings: string[];
}

export default function PolicyWarningsPanel({
  warnings,
}: PolicyWarningsPanelProps) {
  if (warnings.length === 0) return null;

  return (
    <div className="rounded-lg border border-yellow-200 bg-yellow-50 p-4">
      <h3 className="mb-2 text-sm font-semibold text-yellow-800">
        Policy Notices
      </h3>
      <ul className="space-y-1">
        {warnings.map((warning, i) => (
          <li key={i} className="flex items-start gap-2 text-sm text-yellow-700">
            <span className="mt-0.5">&#9888;</span>
            {warning}
          </li>
        ))}
      </ul>
    </div>
  );
}
