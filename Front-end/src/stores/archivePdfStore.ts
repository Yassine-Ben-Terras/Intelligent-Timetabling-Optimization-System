// src/stores/archivePdfStore.ts
import { defineStore } from 'pinia';
import apiClient from '../services/api';
import type { ArchivePDFDTO } from '../types/types';

export const useArchivePdfStore = defineStore('archivePdf', {
  state: () => ({
    pdfList: [] as ArchivePDFDTO[],
    loading: false,
    error: null as string | null
  }),

  actions: {
    async fetchAllPdfs() {
      this.loading = true;
      this.error = null;
      try {
        const { data } = await apiClient.get<ArchivePDFDTO[]>('/pdfs');
        this.pdfList = data;
      } catch (err: any) {
        this.error = err.message;
      } finally {
        this.loading = false;
      }
    },

    async openPdf(id: number) {
      try {
        const { data } = await apiClient.get(`/pdfs/download/${id}`, {
          responseType: 'blob',
          headers: { Accept: 'application/pdf' }
        });
        const blob = new Blob([data], { type: 'application/pdf' });
        const url = URL.createObjectURL(blob);
        window.open(url, '_blank');
      } catch (err) {
        console.error('Erreur ouverture PDF', err);
      }
    }
  }
});
