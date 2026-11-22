export interface Course {
  id: number;
  title: string;
  description?: string;
  category: string;
  level: string;
  skills?: string;
  imageUrl: string;
  durationHours?: number;
  reviews?: string;     // ex. "2.1k avis"
}

export interface Recommendation {
  course: Course;
  score: number;        // 0 a 1
  explanation: string;
  rating?: number;      // 0 a 5
}

export type Verdict = 'LIKE' | 'DISLIKE';
