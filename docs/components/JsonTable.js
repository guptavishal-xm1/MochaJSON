import React from 'react';

const JsonTable = ({ data, title, className = "" }) => {
  if (!data || typeof data !== 'object') {
    return null;
  }

  const entries = Array.isArray(data) 
    ? data.map((item, index) => ({ key: index, value: item }))
    : Object.entries(data).map(([key, value]) => ({ key, value }));

  return (
    <div className={`bg-gray-50 dark:bg-gray-800 rounded-lg p-4 ${className}`}>
      {title && (
        <h4 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
          {title}
        </h4>
      )}
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
          <thead className="bg-gray-100 dark:bg-gray-700">
            <tr>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                Field
              </th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                Type
              </th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                Sample Value
              </th>
            </tr>
          </thead>
          <tbody className="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
            {entries.map(({ key, value }, index) => (
              <tr key={index} className="hover:bg-gray-50 dark:hover:bg-gray-700">
                <td className="px-4 py-3 text-sm font-mono text-gray-900 dark:text-white">
                  {typeof key === 'string' ? `"${key}"` : key}
                </td>
                <td className="px-4 py-3 text-sm text-gray-600 dark:text-gray-300">
                  <code className="bg-gray-100 dark:bg-gray-700 px-2 py-1 rounded text-xs">
                    {getValueType(value)}
                  </code>
                </td>
                <td className="px-4 py-3 text-sm text-gray-900 dark:text-white">
                  <code className="bg-gray-100 dark:bg-gray-700 px-2 py-1 rounded text-xs">
                    {formatValue(value)}
                  </code>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

const getValueType = (value) => {
  if (value === null) return 'null';
  if (Array.isArray(value)) return 'Array';
  if (typeof value === 'object') return 'Object';
  if (typeof value === 'string') return 'String';
  if (typeof value === 'number') return Number.isInteger(value) ? 'int' : 'double';
  if (typeof value === 'boolean') return 'boolean';
  return typeof value;
};

const formatValue = (value) => {
  if (value === null) return 'null';
  if (typeof value === 'string') {
    // Truncate long strings
    return value.length > 50 ? `"${value.substring(0, 50)}..."` : `"${value}"`;
  }
  if (typeof value === 'object') {
    return Array.isArray(value) ? '[...]' : '{...}';
  }
  return String(value);
};

export default JsonTable;
