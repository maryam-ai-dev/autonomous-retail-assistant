export interface RobotPosition {
  x: number;
  y: number;
  theta?: number;
}

export interface NavigationStatus {
  task_id: string;
  destination_label: string;
  destination_x: number;
  destination_y: number;
  status: string;
}

export interface HandoffStatus {
  request_id: string;
  reason: string;
  task_id: string;
  confidence: number;
  location_label: string;
}

export interface RobotState {
  pose: RobotPosition;
  navigation_status: NavigationStatus | null;
  handoff_status: HandoffStatus | null;
  robot_status: string;
}

export interface SendTaskResponse {
  accepted: boolean;
  product_id?: string;
  customer_id?: string;
  reason?: string;
}
