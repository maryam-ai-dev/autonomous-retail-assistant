"use client";

import { useState } from "react";

interface BlockedBrandsProps {
  brands: string[];
  onSave: (brands: string[]) => void;
}

export default function BlockedBrands({ brands, onSave }: BlockedBrandsProps) {
  const [list, setList] = useState<string[]>(brands);
  const [input, setInput] = useState("");
  const [saved, setSaved] = useState(false);

  function handleAdd() {
    const trimmed = input.trim();
    if (trimmed && !list.includes(trimmed)) {
      const updated = [...list, trimmed];
      setList(updated);
      setInput("");
    }
  }

  function handleRemove(brand: string) {
    setList(list.filter((b) => b !== brand));
  }

  function handleSave() {
    onSave(list);
    setSaved(true);
    setTimeout(() => setSaved(false), 2000);
  }

  return (
    <div className="rounded-lg border border-gray-200 bg-white p-4">
      <h3 className="mb-3 text-sm font-semibold">Blocked Brands</h3>
      <div className="mb-3 flex gap-2">
        <input
          type="text"
          placeholder="Add brand..."
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={(e) => e.key === "Enter" && (e.preventDefault(), handleAdd())}
          className="flex-1 rounded border border-gray-300 px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
        <button
          onClick={handleAdd}
          className="rounded bg-gray-100 px-3 py-1.5 text-xs font-medium text-gray-700 hover:bg-gray-200"
        >
          Add
        </button>
      </div>
      {list.length > 0 ? (
        <div className="mb-3 flex flex-wrap gap-2">
          {list.map((brand) => (
            <span
              key={brand}
              className="flex items-center gap-1 rounded-full bg-red-50 px-3 py-1 text-xs text-red-700"
            >
              {brand}
              <button
                onClick={() => handleRemove(brand)}
                className="ml-1 text-red-400 hover:text-red-600"
              >
                &times;
              </button>
            </span>
          ))}
        </div>
      ) : (
        <p className="mb-3 text-sm text-gray-500">No blocked brands</p>
      )}
      <button
        onClick={handleSave}
        className="rounded bg-blue-600 px-4 py-1.5 text-xs font-medium text-white hover:bg-blue-700"
      >
        {saved ? "Saved!" : "Save"}
      </button>
    </div>
  );
}
