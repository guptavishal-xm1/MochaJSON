import React from 'react';

const FeatureCard = ({ icon, title, description, highlight = false }) => {
  return (
    <div className={`feature-card bg-white dark:bg-gray-800 rounded-xl shadow-lg hover:shadow-xl transition-all duration-300 p-6 border ${
      highlight 
        ? 'border-purple-300 dark:border-purple-600 bg-gradient-to-br from-purple-50 to-white dark:from-purple-900/20 dark:to-gray-800' 
        : 'border-gray-200 dark:border-gray-700'
    }`}>
      <div className="flex items-start mb-4">
        <div className={`feature-icon mr-4 p-2 rounded-lg ${
          highlight 
            ? 'bg-purple-100 dark:bg-purple-800 text-purple-600 dark:text-purple-300' 
            : 'bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300'
        }`}>
          {icon}
        </div>
        <h3 className="text-xl font-semibold text-gray-900 dark:text-white flex-1">
          {title}
        </h3>
      </div>
      <p className="text-gray-600 dark:text-gray-300 leading-relaxed">
        {description}
      </p>
    </div>
  );
};

export default FeatureCard;
