import React, { useEffect, useState } from 'react';
import { getDocumentsPaged, downloadDocument, deleteDocument } from '../services/documentService';
import Pagination from '../components/Pagination';

const PAGE_SIZE = 10;
const TYPES = ['ALL', 'IDENTITY', 'POLICY', 'CLAIM', 'OTHER'];

export default function Documents() {
  const [pageData, setPageData] = useState({ content: [], page: 0, totalPages: 0, totalElements: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [type, setType] = useState('ALL');
  const [searchInput, setSearchInput] = useState('');
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);

  useEffect(() => {
    load();
  }, [page, search, type]);

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      const { data } = await getDocumentsPaged({
        page,
        size: PAGE_SIZE,
        search: search || undefined,
        type: type === 'ALL' ? undefined : type,
      });
      setPageData(data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load documents');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    setPage(0);
    setSearch(searchInput);
  };

  const handleTypeChange = (t) => {
    setType(t);
    setPage(0);
  };

  const handleDownload = (doc) => downloadDocument(doc.id, doc.fileName);

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this document?')) return;
    try {
      await deleteDocument(id);
      load();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to delete document');
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-xl font-bold text-gray-800">Document Review</h2>
        <p className="text-sm text-gray-500">Identity, policy, and claim documents uploaded by all customers</p>
      </div>

      <div className="flex flex-wrap items-center gap-3">
        <form onSubmit={handleSearch} className="flex gap-2">
          <input
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
            placeholder="Search by file name or customer"
            className="input-field w-72"
          />
          <button type="submit" className="btn-secondary">Search</button>
        </form>
        <div className="flex gap-2">
          {TYPES.map((t) => (
            <button
              key={t}
              onClick={() => handleTypeChange(t)}
              className={`text-xs font-medium px-3 py-1.5 rounded-full transition-colors ${
                type === t ? 'bg-brand-500 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
              }`}
            >
              {t}
            </button>
          ))}
        </div>
      </div>

      {error && <div className="text-sm text-red-600 bg-red-50 border border-red-100 rounded-lg px-3 py-2">{error}</div>}

      <div className="card overflow-x-auto">
        {loading ? (
          <p className="text-gray-500">Loading documents...</p>
        ) : pageData.content.length === 0 ? (
          <p className="text-gray-500">No documents found.</p>
        ) : (
          <>
            <table className="w-full text-sm">
              <thead>
                <tr className="text-left text-gray-500 border-b">
                  <th className="py-2 pr-4">File Name</th>
                  <th className="py-2 pr-4">Customer</th>
                  <th className="py-2 pr-4">Type</th>
                  <th className="py-2 pr-4">Linked Claim</th>
                  <th className="py-2 pr-4">Uploaded</th>
                  <th className="py-2 pr-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody>
                {pageData.content.map((d) => (
                  <tr key={d.id} className="border-b last:border-0 hover:bg-gray-50">
                    <td className="py-3 pr-4 font-medium text-gray-800">{d.fileName}</td>
                    <td className="py-3 pr-4 text-gray-600">{d.customerName}</td>
                    <td className="py-3 pr-4 text-gray-600">{d.documentType}</td>
                    <td className="py-3 pr-4 text-gray-500">{d.claimPolicyNumber || '—'}</td>
                    <td className="py-3 pr-4 text-gray-500">{new Date(d.uploadedAt).toLocaleDateString()}</td>
                    <td className="py-3 pr-4 text-right space-x-3 whitespace-nowrap">
                      <button onClick={() => handleDownload(d)} className="text-brand-600 hover:underline">Download</button>
                      <button onClick={() => handleDelete(d.id)} className="text-red-600 hover:underline">Delete</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            <Pagination page={pageData.page} totalPages={pageData.totalPages} totalElements={pageData.totalElements} onPageChange={setPage} />
          </>
        )}
      </div>
    </div>
  );
}
