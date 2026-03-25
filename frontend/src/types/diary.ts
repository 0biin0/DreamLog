export interface DiaryCreateRequest {
  title: string;
  content: string;
  unlockDate: string; // yyyy-MM-dd
}

export interface DiaryUpdateRequest {
  title?: string;
  content?: string;
}

export interface DiaryResponse {
  id: number;
  title: string;
  content: string | null;
  unlockDate: string;
  writtenDate: string;
  editDeadline: string;
  locked: boolean;
  daysUntilUnlock: number;
  editable: boolean;
  createdAt: string;
}

export interface DiaryPage {
  content: DiaryResponse[];
  totalPages: number;
  totalElements: number;
  number: number;
  size: number;
}
