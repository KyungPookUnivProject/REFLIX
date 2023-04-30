from sklearn.decomposition import TruncatedSVD
from scipy.sparse.linalg import svds
import json
import matplotlib.pyplot as plt
import seaborn as sns
import pandas as pd
import numpy as np

import sys
def recommend_movie_matrix(tmdbId):
    rating_data = pd.read_csv("ratings_final.csv")
    movie_data = pd.read_csv("movie_final.csv")

    movie_data.drop('genre', axis=1, inplace=True)
    user_movie_data = pd.merge(rating_data, movie_data, on='content_id')

    user_movie_rating = user_movie_data.pivot_table('rating', index = 'userId', columns='tmdbId').fillna(0)

    movie_user_rating = user_movie_rating.values.T

    SVD = TruncatedSVD(n_components=12)
    matrix = SVD.fit_transform(movie_user_rating)

    corr = np.corrcoef(matrix)
    movie_title = user_movie_rating.columns
    movie_title_list = list(movie_title)
    coffey_hands = movie_title_list.index(tmdbId)
    corr_coffey_hands = corr[coffey_hands]
    recommended_movies = list(movie_title[(corr_coffey_hands >= 0.9)])[:50]

    recommended_movies_data = movie_data[movie_data['tmdbId'].isin(recommended_movies)][['tmdbId', 'title']]
    recommended_movies_json = recommended_movies_data.to_json(orient='records')
    return recommended_movies_json
        
    
arg1 = sys.argv[1]
arg1 = int(arg1)

print(recommend_movie_matrix(arg1))