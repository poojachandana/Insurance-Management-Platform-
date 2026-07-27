import React, { useEffect, useState } from 'react';
import { getMyProfile } from '../services/customerService';
import { getDocumentsByCustomer, uploadDocument, downloadDocument, deleteDocument } from '../services/documentService';

const DOC_TYPES = ['IDENTITY', 'POLICY', 'CLAIM', 'OTHER'];

export default function MyDocuments() {
  const [customerId, setCustomerId] = useState(null);
  const [documents, setDocuments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [file, setFile] = useState(null);
  const [docType, setDocType] = useState('IDENTITY');
  const [uploading, setUploading] = useState(false);

  useEffect(() => {
    init();
  }, []);

  const init = async () => {
    setLoading(true);
    setError('');
    try {
      const { data: profile } = await getMyProfile();
      setCustomerId(profile.id);
      const { data: docs } = await getDocumentsByCustomer(profile.id);
      setDocuments(docs);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load documents');
    } finally {
      setLoading(false);
    }
  };

  const handleUpload = async (e) => {
    e.preventDefault();
    if (!file) return;
    setUploading(true);
    setError('');
    try {
      await uploadDocument(customerId, null, docType, file);
      setFile(null);
      const { data: docs } = await getDocumentsByCustomer(customerId);
      setDocuments(docs);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to upload document');
    } finally {
      setUploading(false);
    }
  };

  const handleDownload = (doc) => downloadDocument(doc.id, doc.fileName);

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this document?')) return;
    try {
      await deleteDocument(id);
      setDocuments(documents.filter((d) => d.id !== id));
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to delete document');
    }
  };

  return (
    <div className="space-y-6">
      <h2 className="text-xl font-bold text-gray-800">My Documents</h2>
      {error && <div className="text-sm text-red-600 bg-red-50 border border-red-100 rounded-lg px-3 py-2">{error}</div>}

      <div className="card">
        <h3 className="font-semibold text-gray-700 mb-4">Upload a document</h3>
        <form onSubmit={handleUpload} className="flex flex-wrap items-end gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Document type</label>
            <select value={docType} onChange={(e) => setDocType(e.target.value)} className="input-field">
              {DOC_TYPES.map((t) => <option key={t} value={t}>{t}</option>)}
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">File</label>
            <input type="file" onChange={(e) => setFile(e.target.files[0])} className="text-sm" />
          </div>
          <button type="submit" disabled={uploading || !file} className="btn-primary">
            {uploading ? 'Uploading...' : 'Upload'}
          </button>
        </form>
      </div>

      <div className="card overflow-x-auto">
        {loading ? (
          <p className="text-gray-500">Loading...</p>
        ) : documents.length === 0 ? (
          <p className="text-gray-500">No documents uploaded yet.</p>
        ) : (
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left text-gray-500 border-b">
                <th className="py-2 pr-4">File Name</th>
                <th className="py-2 pr-4">Type</th>
                <th className="py-2 pr-4">Uploaded</th>
                <th className="py-2 pr-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody>
              {documents.map((d) => (
                <tr key={d.id} className="border-b last:border-0">
                  <td className="py-3 pr-4 font-medium text-gray-800">{d.fileName}</td>
                  <td className="py-3 pr-4 text-gray-600">{d.documentType}</td>
                  <td className="py-3 pr-4 text-gray-500">{new Date(d.uploadedAt).toLocaleDateString()}</td>
                  <td className="py-3 pr-4 text-right space-x-3">
                    <button onClick={() => handleDownload(d)} className="text-brand-600 hover:underline">Download</button>
                    <button onClick={() => handleDelete(d.id)} className="text-red-600 hover:underline">Delete</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
