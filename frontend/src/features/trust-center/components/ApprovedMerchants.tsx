"use client";

import apiClient from "@/core/api/client";
import { MERCHANTS } from "@/core/api/endpoints";

interface Merchant {
  id: string;
  name: string;
  sourceType: string;
  approved: boolean;
  trustScore: number;
}

interface ApprovedMerchantsProps {
  merchants: Merchant[];
  onToggle: (id: string, approve: boolean) => void;
}

export default function ApprovedMerchants({
  merchants,
  onToggle,
}: ApprovedMerchantsProps) {
  return (
    <div className="rounded-lg border border-gray-200 bg-white p-4">
      <h3 className="mb-3 text-sm font-semibold">Approved Merchants</h3>
      {merchants.length === 0 ? (
        <p className="text-sm text-gray-500">No merchants found</p>
      ) : (
        <ul className="space-y-2">
          {merchants.map((m) => (
            <li key={m.id} className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium">{m.name}</p>
                <p className="text-xs text-gray-500">
                  {m.sourceType} — Trust: {(m.trustScore * 100).toFixed(0)}%
                </p>
              </div>
              <button
                onClick={() => onToggle(m.id, !m.approved)}
                className={`rounded px-3 py-1 text-xs font-medium ${
                  m.approved
                    ? "bg-green-100 text-green-700 hover:bg-green-200"
                    : "bg-gray-100 text-gray-600 hover:bg-gray-200"
                }`}
              >
                {m.approved ? "Approved" : "Blocked"}
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

export type { Merchant };
