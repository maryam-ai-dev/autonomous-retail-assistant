"use client";

import { RobotState } from "../types";

interface StoreMapProps {
  robotState: RobotState | null;
}

const SCALE = 30;
const STORE_W = 20;
const STORE_H = 15;
const SVG_W = STORE_W * SCALE;
const SVG_H = STORE_H * SCALE;
const PADDING = 20;

const AISLES = [
  { id: "A", xCenter: 2.0, yStart: 3.0, yEnd: 12.0, shelves: [
    { id: "A1", x: 1.0, yStart: 3.0, yEnd: 6.0 },
    { id: "A2", x: 3.0, yStart: 3.0, yEnd: 6.0 },
    { id: "A3", x: 1.0, yStart: 7.0, yEnd: 10.0 },
  ]},
  { id: "B", xCenter: 7.0, yStart: 3.0, yEnd: 12.0, shelves: [
    { id: "B1", x: 5.0, yStart: 3.0, yEnd: 6.0 },
    { id: "B2", x: 7.0, yStart: 3.0, yEnd: 6.0 },
    { id: "B3", x: 5.0, yStart: 7.0, yEnd: 10.0 },
  ]},
  { id: "C", xCenter: 12.0, yStart: 3.0, yEnd: 12.0, shelves: [
    { id: "C1", x: 9.0, yStart: 3.0, yEnd: 6.0 },
    { id: "C2", x: 11.0, yStart: 3.0, yEnd: 6.0 },
    { id: "C3", x: 9.0, yStart: 7.0, yEnd: 10.0 },
  ]},
  { id: "D", xCenter: 17.0, yStart: 3.0, yEnd: 12.0, shelves: [
    { id: "D1", x: 15.0, yStart: 3.0, yEnd: 6.0 },
    { id: "D2", x: 17.0, yStart: 3.0, yEnd: 6.0 },
    { id: "D3", x: 15.0, yStart: 7.0, yEnd: 10.0 },
  ]},
];

function sx(x: number) { return x * SCALE + PADDING; }
function sy(y: number) { return y * SCALE + PADDING; }

export default function StoreMap({ robotState }: StoreMapProps) {
  const navStatus = robotState?.navigation_status;
  const isNavigating = navStatus && navStatus.status !== "COMPLETED";
  const destLabel = navStatus?.destination_label ?? "";

  // Find the target shelf to highlight
  const targetShelfId = (() => {
    if (!isNavigating) return null;
    for (const aisle of AISLES) {
      for (const shelf of aisle.shelves) {
        if (destLabel.includes(shelf.id)) return shelf.id;
      }
    }
    return null;
  })();

  return (
    <div className="rounded-lg border border-gray-200 bg-white p-4">
      <h3 className="mb-2 text-sm font-semibold text-gray-700">Store Map</h3>
      <svg
        viewBox={`0 0 ${SVG_W + PADDING * 2} ${SVG_H + PADDING * 2}`}
        className="w-full"
        aria-label="Top-down store map"
      >
        {/* Store outline */}
        <rect
          x={PADDING} y={PADDING}
          width={SVG_W} height={SVG_H}
          fill="#f9fafb" stroke="#d1d5db" strokeWidth={2}
        />

        {/* Checkout area */}
        <rect
          x={sx(0)} y={sy(0)}
          width={STORE_W * SCALE} height={2 * SCALE}
          fill="#fef3c7" stroke="#f59e0b" strokeWidth={1} opacity={0.6}
        />
        <text x={sx(10)} y={sy(1.2)} textAnchor="middle" className="text-[10px]" fill="#92400e">
          Checkout Area
        </text>

        {/* Entrance */}
        <rect
          x={sx(8)} y={sy(0) - 4}
          width={4 * SCALE} height={8}
          fill="#34d399" rx={2}
        />
        <text x={sx(10)} y={sy(0) + 2} textAnchor="middle" className="text-[8px]" fill="#065f46">
          Entrance
        </text>

        {/* Staff station */}
        <rect
          x={sx(18)} y={sy(12)}
          width={2 * SCALE} height={3 * SCALE}
          fill="#dbeafe" stroke="#3b82f6" strokeWidth={1} opacity={0.6}
        />
        <text x={sx(19)} y={sy(13.7)} textAnchor="middle" className="text-[9px]" fill="#1e40af">
          Staff
        </text>

        {/* Aisles and shelves */}
        {AISLES.map((aisle) => (
          <g key={aisle.id}>
            {/* Aisle label */}
            <text
              x={sx(aisle.xCenter)}
              y={sy(aisle.yEnd + 0.7)}
              textAnchor="middle"
              className="text-[10px] font-semibold"
              fill="#6b7280"
            >
              Aisle {aisle.id}
            </text>

            {/* Shelves */}
            {aisle.shelves.map((shelf) => {
              const isTarget = shelf.id === targetShelfId;
              return (
                <g key={shelf.id}>
                  <rect
                    x={sx(shelf.x) - 10}
                    y={sy(shelf.yStart)}
                    width={20}
                    height={(shelf.yEnd - shelf.yStart) * SCALE}
                    fill={isTarget ? "#fca5a5" : "#e5e7eb"}
                    stroke={isTarget ? "#ef4444" : "#9ca3af"}
                    strokeWidth={isTarget ? 2 : 1}
                    rx={2}
                  />
                  <text
                    x={sx(shelf.x)}
                    y={sy((shelf.yStart + shelf.yEnd) / 2) + 3}
                    textAnchor="middle"
                    className="text-[8px]"
                    fill={isTarget ? "#991b1b" : "#6b7280"}
                  >
                    {shelf.id}
                  </text>
                </g>
              );
            })}
          </g>
        ))}

        {/* Navigation destination marker */}
        {isNavigating && navStatus && (
          <circle
            cx={sx(navStatus.destination_x)}
            cy={sy(navStatus.destination_y)}
            r={6}
            fill="none"
            stroke="#ef4444"
            strokeWidth={2}
            strokeDasharray="3,3"
          >
            <animate attributeName="r" values="6;10;6" dur="1.5s" repeatCount="indefinite" />
          </circle>
        )}

        {/* Robot position */}
        {robotState && (
          <circle
            cx={sx(robotState.pose.x)}
            cy={sy(robotState.pose.y)}
            r={7}
            fill="#3b82f6"
            stroke="#1d4ed8"
            strokeWidth={2}
          >
            <animate attributeName="opacity" values="1;0.6;1" dur="2s" repeatCount="indefinite" />
          </circle>
        )}
      </svg>
    </div>
  );
}
