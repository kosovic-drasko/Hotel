package hoteladmin.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import hoteladmin.IntegrationTest;
import hoteladmin.domain.Proba;
import hoteladmin.repository.ProbaRepository;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link ProbaResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ProbaResourceIT {

    private static final String DEFAULT_PROBA = "AAAAAAAAAA";
    private static final String UPDATED_PROBA = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/probas";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ProbaRepository probaRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restProbaMockMvc;

    private Proba proba;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Proba createEntity(EntityManager em) {
        Proba proba = new Proba().proba(DEFAULT_PROBA);
        return proba;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Proba createUpdatedEntity(EntityManager em) {
        Proba proba = new Proba().proba(UPDATED_PROBA);
        return proba;
    }

    @BeforeEach
    public void initTest() {
        proba = createEntity(em);
    }

    @Test
    @Transactional
    void createProba() throws Exception {
        int databaseSizeBeforeCreate = probaRepository.findAll().size();
        // Create the Proba
        restProbaMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(proba)))
            .andExpect(status().isCreated());

        // Validate the Proba in the database
        List<Proba> probaList = probaRepository.findAll();
        assertThat(probaList).hasSize(databaseSizeBeforeCreate + 1);
        Proba testProba = probaList.get(probaList.size() - 1);
        assertThat(testProba.getProba()).isEqualTo(DEFAULT_PROBA);
    }

    @Test
    @Transactional
    void createProbaWithExistingId() throws Exception {
        // Create the Proba with an existing ID
        proba.setId(1L);

        int databaseSizeBeforeCreate = probaRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restProbaMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(proba)))
            .andExpect(status().isBadRequest());

        // Validate the Proba in the database
        List<Proba> probaList = probaRepository.findAll();
        assertThat(probaList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllProbas() throws Exception {
        // Initialize the database
        probaRepository.saveAndFlush(proba);

        // Get all the probaList
        restProbaMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(proba.getId().intValue())))
            .andExpect(jsonPath("$.[*].proba").value(hasItem(DEFAULT_PROBA)));
    }

    @Test
    @Transactional
    void getProba() throws Exception {
        // Initialize the database
        probaRepository.saveAndFlush(proba);

        // Get the proba
        restProbaMockMvc
            .perform(get(ENTITY_API_URL_ID, proba.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(proba.getId().intValue()))
            .andExpect(jsonPath("$.proba").value(DEFAULT_PROBA));
    }

    @Test
    @Transactional
    void getNonExistingProba() throws Exception {
        // Get the proba
        restProbaMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingProba() throws Exception {
        // Initialize the database
        probaRepository.saveAndFlush(proba);

        int databaseSizeBeforeUpdate = probaRepository.findAll().size();

        // Update the proba
        Proba updatedProba = probaRepository.findById(proba.getId()).get();
        // Disconnect from session so that the updates on updatedProba are not directly saved in db
        em.detach(updatedProba);
        updatedProba.proba(UPDATED_PROBA);

        restProbaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedProba.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedProba))
            )
            .andExpect(status().isOk());

        // Validate the Proba in the database
        List<Proba> probaList = probaRepository.findAll();
        assertThat(probaList).hasSize(databaseSizeBeforeUpdate);
        Proba testProba = probaList.get(probaList.size() - 1);
        assertThat(testProba.getProba()).isEqualTo(UPDATED_PROBA);
    }

    @Test
    @Transactional
    void putNonExistingProba() throws Exception {
        int databaseSizeBeforeUpdate = probaRepository.findAll().size();
        proba.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProbaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, proba.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(proba))
            )
            .andExpect(status().isBadRequest());

        // Validate the Proba in the database
        List<Proba> probaList = probaRepository.findAll();
        assertThat(probaList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchProba() throws Exception {
        int databaseSizeBeforeUpdate = probaRepository.findAll().size();
        proba.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProbaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(proba))
            )
            .andExpect(status().isBadRequest());

        // Validate the Proba in the database
        List<Proba> probaList = probaRepository.findAll();
        assertThat(probaList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamProba() throws Exception {
        int databaseSizeBeforeUpdate = probaRepository.findAll().size();
        proba.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProbaMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(proba)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Proba in the database
        List<Proba> probaList = probaRepository.findAll();
        assertThat(probaList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateProbaWithPatch() throws Exception {
        // Initialize the database
        probaRepository.saveAndFlush(proba);

        int databaseSizeBeforeUpdate = probaRepository.findAll().size();

        // Update the proba using partial update
        Proba partialUpdatedProba = new Proba();
        partialUpdatedProba.setId(proba.getId());

        partialUpdatedProba.proba(UPDATED_PROBA);

        restProbaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedProba.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedProba))
            )
            .andExpect(status().isOk());

        // Validate the Proba in the database
        List<Proba> probaList = probaRepository.findAll();
        assertThat(probaList).hasSize(databaseSizeBeforeUpdate);
        Proba testProba = probaList.get(probaList.size() - 1);
        assertThat(testProba.getProba()).isEqualTo(UPDATED_PROBA);
    }

    @Test
    @Transactional
    void fullUpdateProbaWithPatch() throws Exception {
        // Initialize the database
        probaRepository.saveAndFlush(proba);

        int databaseSizeBeforeUpdate = probaRepository.findAll().size();

        // Update the proba using partial update
        Proba partialUpdatedProba = new Proba();
        partialUpdatedProba.setId(proba.getId());

        partialUpdatedProba.proba(UPDATED_PROBA);

        restProbaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedProba.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedProba))
            )
            .andExpect(status().isOk());

        // Validate the Proba in the database
        List<Proba> probaList = probaRepository.findAll();
        assertThat(probaList).hasSize(databaseSizeBeforeUpdate);
        Proba testProba = probaList.get(probaList.size() - 1);
        assertThat(testProba.getProba()).isEqualTo(UPDATED_PROBA);
    }

    @Test
    @Transactional
    void patchNonExistingProba() throws Exception {
        int databaseSizeBeforeUpdate = probaRepository.findAll().size();
        proba.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProbaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, proba.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(proba))
            )
            .andExpect(status().isBadRequest());

        // Validate the Proba in the database
        List<Proba> probaList = probaRepository.findAll();
        assertThat(probaList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchProba() throws Exception {
        int databaseSizeBeforeUpdate = probaRepository.findAll().size();
        proba.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProbaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(proba))
            )
            .andExpect(status().isBadRequest());

        // Validate the Proba in the database
        List<Proba> probaList = probaRepository.findAll();
        assertThat(probaList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamProba() throws Exception {
        int databaseSizeBeforeUpdate = probaRepository.findAll().size();
        proba.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProbaMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(proba)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Proba in the database
        List<Proba> probaList = probaRepository.findAll();
        assertThat(probaList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteProba() throws Exception {
        // Initialize the database
        probaRepository.saveAndFlush(proba);

        int databaseSizeBeforeDelete = probaRepository.findAll().size();

        // Delete the proba
        restProbaMockMvc
            .perform(delete(ENTITY_API_URL_ID, proba.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Proba> probaList = probaRepository.findAll();
        assertThat(probaList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
