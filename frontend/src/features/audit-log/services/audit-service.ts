import apiClient from "@/core/api/client";
import { AUDIT } from "@/core/api/endpoints";

export interface AuditEvent {
  id: string;
  eventType: string;
  entityType: string;
  entityId: string;
  payload: Record<string, unknown>;
  createdAt: string;
}

export async function getAuditEvents(type?: string): Promise<AuditEvent[]> {
  const params = type ? { type } : {};
  const { data } = await apiClient.get<AuditEvent[]>(AUDIT.BASE, { params });
  return data;
}
