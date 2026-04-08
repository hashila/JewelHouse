import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { MetalLivePriceResponseDto, MetalResponseDto } from './metal-api.models';

@Injectable({ providedIn: 'root' })
export class MetalApiService {
  private readonly baseUrl = 'http://localhost:8080/api/metals';

  private readonly livePricesSubject = new BehaviorSubject<MetalLivePriceResponseDto[]>([]);
  readonly livePrices$ = this.livePricesSubject.asObservable();

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

  /** GET /api/metals/getLivePrices — loads and caches the latest list. */
  getLivePrices(): Observable<MetalLivePriceResponseDto[]> {
    return this.http.get<MetalLivePriceResponseDto[]>(`${this.baseUrl}/getLivePrices`).pipe(
      tap((list) => this.livePricesSubject.next(list ?? []))
    );
  }

  getCachedLivePrices(): MetalLivePriceResponseDto[] {
    return this.livePricesSubject.value;
  }
}
