import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { MetalResponseDto } from './metal-api.models';

@Injectable({ providedIn: 'root' })
export class MetalApiService {
  private readonly baseUrl = 'http://localhost:8080/api/metals';

  private readonly metalListSubject = new BehaviorSubject<MetalResponseDto[]>([]);
  readonly metalList$ = this.metalListSubject.asObservable();

  constructor(private readonly http: HttpClient) {}

  /** GET /api/metals/getList — catalog of metals (id + name). */
  getList(): Observable<MetalResponseDto[]> {
    return this.http.get<MetalResponseDto[]>(`${this.baseUrl}/getList`).pipe(
      tap((list) => this.metalListSubject.next(list ?? []))
    );
  }

  getCachedMetalList(): MetalResponseDto[] {
    return this.metalListSubject.value;
  }
}
