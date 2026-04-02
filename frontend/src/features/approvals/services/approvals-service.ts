import apiClient from "@/core/api/client";
import { APPROVALS } from "@/core/api/endpoints";

export interface ApprovalRequest {
  id: string;
  userId: string;
  cartId: string;
  status: string;
  triggerReason: string;
  totalAmount: number;
  requiresUserAction: boolean;
  createdAt: string;
  decidedAt: string | null;
  decision: string | null;
}

export async function getApprovals(): Promise<ApprovalRequest[]> {
  const { data } = await apiClient.get<ApprovalRequest[]>(APPROVALS.BASE);
  return data;
}

export async function approve(id: string): Promise<ApprovalRequest> {
  const { data } = await apiClient.post<ApprovalRequest>(
    `${APPROVALS.BASE}/${id}/approve`
  );
  return data;
}

export async function reject(id: string): Promise<ApprovalRequest> {
  const { data } = await apiClient.post<ApprovalRequest>(
    `${APPROVALS.BASE}/${id}/reject`
  );
  return data;
}
